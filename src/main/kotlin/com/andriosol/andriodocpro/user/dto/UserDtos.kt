package com.andriosol.andriodocpro.user.dto

import com.andriosol.andriodocpro.user.entity.User
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

@Serdeable
data class RegisterRequest(
    @field:NotBlank @field:Size(min = 3, max = 100) val username: String,
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8, max = 255) val password: String
)

/** Never includes the password or its hash. */
@Serdeable
data class RegisterResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val createdAt: Instant?
)

fun User.toResponse() = RegisterResponse(id!!, username, email, createdAt)

@Serdeable
data class ForgotPasswordRequest(
    @field:NotBlank @field:Email val email: String
)

@Serdeable
data class ResetPasswordRequest(
    @field:NotBlank val token: String,
    @field:NotBlank @field:Size(min = 8, max = 255) val newPassword: String
)

@Serdeable
data class MessageResponse(val message: String)
