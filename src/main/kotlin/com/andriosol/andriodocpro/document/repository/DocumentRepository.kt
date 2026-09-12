package com.andriosol.andriodocpro.document.repository

import com.andriosol.andriodocpro.document.entity.Document
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface DocumentRepository : CrudRepository<Document, UUID> {
    fun findByIdIn(ids: Collection<UUID>): List<Document>
    fun findByOwnerId(ownerId: UUID): List<Document>
}
