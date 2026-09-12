package com.andriosol.andriodocpro.document.entity

import com.andriosol.andriodocpro.common.entity.BaseEntity
import io.micronaut.data.annotation.MappedEntity
import java.util.UUID

/**
 * Metadata for an ingested source document. The actual text is normalised into
 * [com.andriosol.andriodocpro.chunk.entity.DocumentChunk] rows (one per chunk, each with an
 * embedding) so it can be searched efficiently by the RAG pipeline.
 *
 * Visible only to [ownerId], users it has been shared with (see DocumentShare), and admins —
 * enforced by [com.andriosol.andriodocpro.document.service.DocumentAccessService].
 */
@MappedEntity("documents")
class Document(
    var filename: String,
    var contentType: String,
    var fileSize: Long,
    var charCount: Int,
    var chunkCount: Int,
    var ownerId: UUID
) : BaseEntity()
