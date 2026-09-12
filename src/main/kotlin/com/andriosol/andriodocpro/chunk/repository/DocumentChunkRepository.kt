package com.andriosol.andriodocpro.chunk.repository

import com.andriosol.andriodocpro.chunk.entity.DocumentChunk
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface DocumentChunkRepository : CrudRepository<DocumentChunk, UUID> {
    fun findByDocumentIdOrderByChunkIndex(documentId: UUID): List<DocumentChunk>
}
