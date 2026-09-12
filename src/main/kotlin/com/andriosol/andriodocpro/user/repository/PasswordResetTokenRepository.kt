package com.andriosol.andriodocpro.user.repository

import com.andriosol.andriodocpro.user.entity.PasswordResetToken
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import java.util.Optional
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface PasswordResetTokenRepository : CrudRepository<PasswordResetToken, UUID> {
    fun findByTokenHash(tokenHash: String): Optional<PasswordResetToken>
}
