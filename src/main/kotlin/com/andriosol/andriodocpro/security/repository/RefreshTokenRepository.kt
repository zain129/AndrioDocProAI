package com.andriosol.andriodocpro.security.repository

import com.andriosol.andriodocpro.security.entity.RefreshTokenEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import java.util.Optional
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface RefreshTokenRepository : CrudRepository<RefreshTokenEntity, UUID> {
    fun findByRefreshToken(refreshToken: String): Optional<RefreshTokenEntity>
    fun updateByUsername(username: String, revoked: Boolean): Long
}
