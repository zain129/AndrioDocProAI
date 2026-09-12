package com.andriosol.andriodocpro.rag.client

import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import kotlin.math.sqrt

/**
 * Dependency-free fallback embedding: a normalised hashed bag-of-words vector.
 * It is deterministic and self-consistent (good enough to exercise the retrieval pipeline
 * end-to-end without an external service) but is NOT semantically meaningful — switch to the
 * Hugging Face provider (`rag.embedding.provider=huggingface`) for real quality.
 */
@Singleton
@Requires(property = "rag.embedding.provider", value = "local", defaultValue = "local")
class LocalEmbeddingClient(
    @Value("\${rag.embedding.dimensions:384}") override val dimensions: Int
) : EmbeddingClient {

    init {
        log.warn(
            "Using LOCAL hash-based embeddings ({} dims) — deterministic but not semantic. " +
                "Set rag.embedding.provider=huggingface for real embeddings.", dimensions
        )
    }

    override fun embed(texts: List<String>): List<FloatArray> {
        log.debug("Locally embedding {} texts", texts.size)
        return texts.map { embedOne(it) }
    }

    private fun embedOne(text: String): FloatArray {
        val vector = FloatArray(dimensions)
        for (token in text.lowercase().split(Regex("[^a-z0-9]+"))) {
            if (token.isBlank()) continue
            val bucket = Math.floorMod(token.hashCode(), dimensions)
            vector[bucket] += 1f
        }
        var norm = 0.0
        for (v in vector) norm += (v * v).toDouble()
        norm = sqrt(norm)
        if (norm > 0) {
            for (i in vector.indices) vector[i] = (vector[i] / norm).toFloat()
        }
        return vector
    }

    companion object {
        private val log = LoggerFactory.getLogger(LocalEmbeddingClient::class.java)
    }
}
