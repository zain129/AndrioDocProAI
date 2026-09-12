package com.andriosol.andriodocpro.chunk.entity

import com.andriosol.andriodocpro.common.entity.BaseEntity
import io.micronaut.data.annotation.MappedEntity
import java.util.UUID

/**
 * A contiguous slice of a document's text. The `embedding` column (pgvector) is written and
 * queried via [com.andriosol.andriodocpro.chunk.repository.ChunkStore] using native SQL,
 * so it is intentionally not mapped here.
 */
@MappedEntity("document_chunks")
class DocumentChunk(
    var documentId: UUID,
    var chunkIndex: Int,
    var content: String
) : BaseEntity()
