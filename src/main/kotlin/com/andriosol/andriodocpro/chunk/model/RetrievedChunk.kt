package com.andriosol.andriodocpro.chunk.model

import java.util.UUID

/** A chunk returned from a similarity search, with its cosine similarity [score] (0..1). */
data class RetrievedChunk(
    val documentId: UUID,
    val filename: String,
    val chunkIndex: Int,
    val content: String,
    val score: Double
)
