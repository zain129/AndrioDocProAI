package com.andriosol.andriodocpro.user.entity

import com.andriosol.andriodocpro.common.entity.BaseEntity
import io.micronaut.data.annotation.MappedEntity
import java.time.Instant
import java.util.UUID

/**
 * A pending password-reset request. [tokenHash] is a SHA-256 digest of the random token
 * that was actually sent to the user — only the digest is stored, so a database leak alone
 * cannot be used to reset anyone's password.
 */
@MappedEntity("password_reset_tokens")
class PasswordResetToken(
    var userId: UUID,
    var tokenHash: String,
    var expiresAt: Instant,
    var used: Boolean = false
) : BaseEntity()
