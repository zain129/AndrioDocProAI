package com.andriosol.andriodocpro.document.dto

import com.andriosol.andriodocpro.document.entity.Document
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

/** View returned by upload / list / get endpoints. */
@Serdeable
data class DocumentResponse(
    val id: UUID,
    val filename: String,
    val contentType: String,
    val fileSize: Long,
    val charCount: Int,
    val chunkCount: Int,
    val createdAt: Instant?,
    /** True if the requesting user owns this document (vs. it being shared with them). */
    val owned: Boolean
)

/** A single stored chunk of a document. */
@Serdeable
data class ChunkView(
    val chunkIndex: Int,
    val content: String
)

@Serdeable
data class ShareRequest(
    @field:NotBlank @field:Email val email: String
)

@Serdeable
data class ShareResponse(
    val email: String,
    val alreadyRegistered: Boolean,
    val message: String
)

@Serdeable
data class ShareView(
    val email: String,
    val sharedAt: Instant?
)

fun Document.toResponse(owned: Boolean) = DocumentResponse(
    id = id!!,
    filename = filename,
    contentType = contentType,
    fileSize = fileSize,
    charCount = charCount,
    chunkCount = chunkCount,
    createdAt = createdAt,
    owned = owned
)
