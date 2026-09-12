package com.andriosol.andriodocpro.document.service

import com.andriosol.andriodocpro.chunk.repository.ChunkStore
import com.andriosol.andriodocpro.chunk.repository.DocumentChunkRepository
import com.andriosol.andriodocpro.chunk.service.Chunker
import com.andriosol.andriodocpro.document.dto.ChunkView
import com.andriosol.andriodocpro.document.dto.DocumentResponse
import com.andriosol.andriodocpro.document.dto.toResponse
import com.andriosol.andriodocpro.document.entity.Document
import com.andriosol.andriodocpro.document.repository.DocumentRepository
import com.andriosol.andriodocpro.rag.client.EmbeddingClient
import com.andriosol.andriodocpro.user.entity.User
import com.andriosol.andriodocpro.user.entity.isAdmin
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.http.multipart.CompletedFileUpload
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val documentChunkRepository: DocumentChunkRepository,
    private val textExtractor: TextExtractor,
    private val chunker: Chunker,
    private val embeddingClient: EmbeddingClient,
    private val chunkStore: ChunkStore,
    private val documentAccessService: DocumentAccessService
) {

    /**
     * Extracts text from an uploaded PDF/DOC/DOCX, splits it into chunks, embeds each chunk,
     * and persists the document metadata (owned by [owner]) plus the embedded chunks.
     */
    fun store(file: CompletedFileUpload, owner: User): DocumentResponse {
        val filename = file.filename?.takeIf { it.isNotBlank() }
            ?: throw HttpStatusException(HttpStatus.BAD_REQUEST, "A filename is required")
        log.info("Ingesting upload '{}' ({} bytes) for user '{}'", filename, file.size, owner.username)

        val extension = filename.substringAfterLast('.', "").lowercase()
        if (extension !in SUPPORTED_EXTENSIONS) {
            log.warn("Rejected '{}': unsupported extension '.{}'", filename, extension)
            throw HttpStatusException(
                HttpStatus.BAD_REQUEST,
                "Unsupported file type '.$extension'. Allowed: ${SUPPORTED_EXTENSIONS.joinToString(", ")}"
            )
        }

        val bytes = file.bytes
        if (bytes.isEmpty()) {
            log.warn("Rejected '{}': empty file", filename)
            throw HttpStatusException(HttpStatus.BAD_REQUEST, "The uploaded file is empty")
        }

        val text = try {
            textExtractor.extract(bytes.inputStream(), filename)
        } catch (e: Exception) {
            log.error("Text extraction failed for '{}'", filename, e)
            throw HttpStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Failed to extract text from '$filename': ${e.message}"
            )
        }.trim()
        log.debug("Extracted {} chars from '{}'", text.length, filename)

        val chunks = chunker.chunk(text)
        if (chunks.isEmpty()) {
            log.warn("Rejected '{}': no extractable text", filename)
            throw HttpStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No extractable text was found in '$filename'")
        }
        log.debug("Split '{}' into {} chunks", filename, chunks.size)

        val embedStart = System.currentTimeMillis()
        val embeddings = try {
            embeddingClient.embed(chunks)
        } catch (e: Exception) {
            log.error("Embedding failed for '{}' ({} chunks)", filename, chunks.size, e)
            throw HttpStatusException(HttpStatus.BAD_GATEWAY, "Failed to generate embeddings: ${e.message}")
        }
        log.debug("Embedded {} chunks for '{}' in {} ms", embeddings.size, filename, System.currentTimeMillis() - embedStart)

        val saved = documentRepository.save(
            Document(
                filename = filename,
                contentType = file.contentType.map { it.name }.orElse("application/octet-stream"),
                fileSize = bytes.size.toLong(),
                charCount = text.length,
                chunkCount = chunks.size,
                ownerId = owner.id!!
            )
        )

        try {
            chunkStore.insertAll(saved.id!!, chunks, embeddings)
        } catch (e: Exception) {
            log.error("Chunk persistence failed for document {} ('{}'); rolling back document row", saved.id, filename, e)
            documentRepository.deleteById(saved.id!!)
            throw HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store document chunks: ${e.message}")
        }

        log.info("Ingested document {} ('{}'): {} chars, {} chunks", saved.id, filename, text.length, chunks.size)
        return saved.toResponse(owned = true)
    }

    /** Every document owned by [user], shared with them, or (for admins) every document. */
    fun list(user: User): List<DocumentResponse> {
        val ids = documentAccessService.accessibleDocumentIds(user)
        val documents = if (ids == null) documentRepository.findAll() else documentRepository.findByIdIn(ids)
        return documents.map { it.toResponse(owned = user.isAdmin || it.ownerId == user.id) }
    }

    fun get(id: UUID, user: User): DocumentResponse {
        val document = findOrThrow(id)
        documentAccessService.requireViewAccess(document, user)
        return document.toResponse(owned = user.isAdmin || document.ownerId == user.id)
    }

    fun chunks(id: UUID, user: User): List<ChunkView> {
        val document = findOrThrow(id)
        documentAccessService.requireViewAccess(document, user)
        return documentChunkRepository.findByDocumentIdOrderByChunkIndex(id)
            .map { ChunkView(it.chunkIndex, it.content) }
    }

    /** Deletes a document; its chunks and shares are removed via ON DELETE CASCADE. */
    fun delete(id: UUID, user: User) {
        val document = findOrThrow(id)
        documentAccessService.requireManageAccess(document, user)
        documentRepository.deleteById(id)
        log.info("Deleted document {} (chunks/shares removed via cascade)", id)
    }

    internal fun findOrThrow(id: UUID): Document =
        documentRepository.findById(id).orElseThrow {
            HttpStatusException(HttpStatus.NOT_FOUND, "Document $id not found")
        }

    companion object {
        private val log = LoggerFactory.getLogger(DocumentService::class.java)
        val SUPPORTED_EXTENSIONS = setOf("pdf", "doc", "docx")
    }
}
