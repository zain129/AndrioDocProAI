package com.andriosol.andriodocpro.user.repository

import com.andriosol.andriodocpro.user.entity.User
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.CrudRepository
import java.util.Optional
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface UserRepository : CrudRepository<User, UUID> {
    fun findByUsername(username: String): Optional<User>
    fun findByEmail(email: String): Optional<User>
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}
