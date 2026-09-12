package com.andriosol.andriodocpro.rag.client

/** Produces dense vector embeddings for text. Implementations are selected via `rag.embedding.provider`. */
interface EmbeddingClient {
    /** Embeds each input, returning one [FloatArray] of length [dimensions] per input, in order. */
    fun embed(texts: List<String>): List<FloatArray>

    val dimensions: Int
}
