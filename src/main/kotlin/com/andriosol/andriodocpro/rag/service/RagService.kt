package com.andriosol.andriodocpro.rag.service

import com.andriosol.andriodocpro.chunk.model.RetrievedChunk
import com.andriosol.andriodocpro.chunk.repository.ChunkStore
import com.andriosol.andriodocpro.document.service.DocumentAccessService
import com.andriosol.andriodocpro.document.service.DocumentService
import com.andriosol.andriodocpro.rag.client.EmbeddingClient
import com.andriosol.andriodocpro.rag.client.LlmClient
import com.andriosol.andriodocpro.rag.dto.AnswerResponse
import com.andriosol.andriodocpro.rag.dto.Source
import com.andriosol.andriodocpro.user.entity.User
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Retrieval-Augmented Generation: embed the question, find the most similar chunks
 * (optionally within one document), and ask the LLM to answer using only that context.
 */
@Singleton
class RagService(
    private val embeddingClient: EmbeddingClient,
    private val chunkStore: ChunkStore,
    private val llmClient: LlmClient,
    private val documentService: DocumentService,
    private val documentAccessService: DocumentAccessService,
    @Value("\${rag.retrieval.top-k:5}") private val topK: Int
) {

    fun answerForDocument(documentId: UUID, question: String, user: User): AnswerResponse {
        log.info("Question received (scope: document {}, user '{}')", documentId, user.username)
        val document = documentService.findOrThrow(documentId)
        documentAccessService.requireViewAccess(document, user)
        return buildAnswer(question, retrieve(question, setOf(documentId)))
    }

    fun answerAcrossDocuments(question: String, user: User): AnswerResponse {
        log.info("Question received (scope: all accessible documents, user '{}')", user.username)
        val accessibleIds = documentAccessService.accessibleDocumentIds(user)
        return buildAnswer(question, retrieve(question, accessibleIds))
    }

    /** [documentIds] is passed straight through to ChunkStore: null = unrestricted (admin). */
    private fun retrieve(question: String, documentIds: Set<UUID>?): List<RetrievedChunk> {
        if (documentIds != null && documentIds.isEmpty()) return emptyList()
        log.debug("Retrieving top-{} chunks for question ({} chars)", topK, question.length)
        val queryEmbedding = embeddingClient.embed(listOf(question)).firstOrNull() ?: return emptyList()
        val hits = chunkStore.search(queryEmbedding, topK, documentIds)
        if (log.isDebugEnabled) {
            log.debug("Retrieved {} chunks: {}", hits.size,
                hits.joinToString { "${it.filename}#${it.chunkIndex}=%.4f".format(it.score) })
        }
        return hits
    }

    private fun buildAnswer(question: String, hits: List<RetrievedChunk>): AnswerResponse {
        if (hits.isEmpty()) {
            log.info("No relevant chunks found; returning empty answer")
            return AnswerResponse(question, "I couldn't find any relevant content to answer that.", emptyList())
        }

        val context = hits.mapIndexed { i, hit -> "[${i + 1}] (${hit.filename}) ${hit.content}" }
            .joinToString("\n\n")
        val system = "You are a precise assistant for the AndrioDocPro platform. " +
            "Answer the user's question using ONLY the provided context. " +
            "If the answer is not contained in the context, say you don't know. " +
            "Cite the sources you rely on as [n]."
        val user = "Context:\n$context\n\nQuestion: $question"

        val llmStart = System.currentTimeMillis()
        val answer = llmClient.answer(system, user)
        log.info(
            "Answer generated in {} ms ({} context chunks, {} answer chars)",
            System.currentTimeMillis() - llmStart, hits.size, answer.length
        )
        val sources = hits.map {
            Source(
                documentId = it.documentId,
                filename = it.filename,
                chunkIndex = it.chunkIndex,
                excerpt = it.content.take(300),
                score = it.score
            )
        }
        return AnswerResponse(question, answer, sources)
    }

    companion object {
        private val log = LoggerFactory.getLogger(RagService::class.java)
    }
}
