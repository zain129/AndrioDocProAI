package com.andriosol.andriodocpro.document.repository

import com.andriosol.andriodocpro.document.entity.DocumentShare
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import java.util.Optional
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface DocumentShareRepository : CrudRepository<DocumentShare, UUID> {
    fun existsByDocumentIdAndEmail(documentId: UUID, email: String): Boolean
    fun findByDocumentIdAndEmail(documentId: UUID, email: String): Optional<DocumentShare>
    fun findByDocumentIdOrderByCreatedAt(documentId: UUID): List<DocumentShare>
    fun findByEmail(email: String): List<DocumentShare>
    fun deleteByDocumentIdAndEmail(documentId: UUID, email: String): Long
}
