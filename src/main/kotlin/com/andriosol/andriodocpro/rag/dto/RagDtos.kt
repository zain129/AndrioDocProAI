package com.andriosol.andriodocpro.rag.dto

import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank
import java.util.UUID

@Serdeable
data class AskRequest(
    @field:NotBlank val question: String
)

/** A chunk that contributed to the answer, for citation/traceability. */
@Serdeable
data class Source(
    val documentId: UUID,
    val filename: String,
    val chunkIndex: Int,
    val excerpt: String,
    val score: Double
)

@Serdeable
data class AnswerResponse(
    val question: String,
    val answer: String,
    val sources: List<Source>
)
