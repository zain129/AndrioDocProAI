package com.andriosol.andriodocpro.rag.client

import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.core.type.Argument
import io.micronaut.serde.ObjectMapper
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Embeds text via the Hugging Face feature-extraction endpoint. Defaults to
 * sentence-transformers/all-MiniLM-L6-v2 (384 dims, matching the pgvector column).
 */
@Singleton
@Requires(property = "rag.embedding.provider", value = "huggingface")
class HuggingFaceEmbeddingClient(
    private val objectMapper: ObjectMapper,
    @Value("\${rag.embedding.dimensions:384}") override val dimensions: Int,
    @Value("\${rag.embedding.model:sentence-transformers/all-MiniLM-L6-v2}") private val model: String,
    @Value("\${rag.huggingface.embeddings-url:https://router.huggingface.co/hf-inference/models}") private val baseUrl: String,
    @Value("\${rag.huggingface.api-token:}") private val token: String
) : EmbeddingClient {

    private val http: HttpClient = HttpClient.newHttpClient()

    override fun embed(texts: List<String>): List<FloatArray> {
        check(token.isNotBlank()) {
            "rag.huggingface.api-token (env HF_TOKEN) is required for the huggingface embedding provider"
        }
        if (texts.isEmpty()) return emptyList()

        val payload = objectMapper.writeValueAsString(
            mapOf("inputs" to texts, "options" to mapOf("wait_for_model" to true))
        )
        val request = HttpRequest.newBuilder(URI.create("$baseUrl/$model/pipeline/feature-extraction"))
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build()

        log.debug("Calling HF embedding model '{}' with {} inputs", model, texts.size)
        val start = System.currentTimeMillis()
        val response = http.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            log.error("HF embeddings request to '{}' failed with HTTP {} after {} ms", model, response.statusCode(), System.currentTimeMillis() - start)
        }
        check(response.statusCode() in 200..299) {
            "Hugging Face embeddings request failed (${response.statusCode()}): ${response.body()}"
        }

        val rows: List<List<Double>> = objectMapper.readValue(
            response.body(),
            Argument.listOf(Argument.listOf(Double::class.javaObjectType))
        )
        log.debug("HF embedded {} inputs in {} ms", rows.size, System.currentTimeMillis() - start)
        return rows.map { row -> FloatArray(row.size) { row[it].toFloat() } }
    }

    companion object {
        private val log = LoggerFactory.getLogger(HuggingFaceEmbeddingClient::class.java)
    }
}
