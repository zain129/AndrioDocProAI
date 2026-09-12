package com.andriosol.andriodocpro.user.service

import com.andriosol.andriodocpro.notification.EmailSender
import com.andriosol.andriodocpro.security.service.PasswordHasher
import com.andriosol.andriodocpro.user.repository.PasswordResetTokenRepository
import com.andriosol.andriodocpro.user.repository.UserRepository
import com.andriosol.andriodocpro.user.entity.PasswordResetToken
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64

/**
 * Issues and redeems password-reset tokens. A reset token is a high-entropy random value;
 * only its SHA-256 digest is ever persisted, so a database compromise alone cannot be used
 * to hijack an account. The response to [forgotPassword] never reveals whether the email
 * exists, to avoid leaking which addresses have accounts.
 */
@Singleton
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordHasher: PasswordHasher,
    private val emailSender: EmailSender,
    @Value("\${app.security.password-reset.expiry-minutes:30}") private val expiryMinutes: Long,
    @Value("\${app.frontend-base-url:http://localhost:5173}") private val frontendBaseUrl: String
) {

    fun forgotPassword(rawEmail: String) {
        val email = rawEmail.trim().lowercase()
        val user = userRepository.findByEmail(email).orElse(null)
        if (user == null) {
            log.info("Password reset requested for an email with no matching account")
            return
        }

        val rawToken = generateRawToken()
        passwordResetTokenRepository.save(
            PasswordResetToken(
                userId = user.id!!,
                tokenHash = sha256Hex(rawToken),
                expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES)
            )
        )
        log.info("Password reset token issued for user '{}'", user.username)
        val resetLink = "$frontendBaseUrl/reset-password?token=$rawToken"
        emailSender.send(user.email, "Reset your AndrioDocPro AI password", "Reset your password: $resetLink")
    }

    fun resetPassword(rawToken: String, newPassword: String) {
        val entry = passwordResetTokenRepository.findByTokenHash(sha256Hex(rawToken)).orElse(null)
        if (entry == null || entry.used || entry.expiresAt.isBefore(Instant.now())) {
            log.warn("Password reset rejected: invalid, used, or expired token")
            throw HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token")
        }

        val user = userRepository.findById(entry.userId).orElseThrow {
            HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token")
        }
        user.passwordHash = passwordHasher.hash(newPassword)
        userRepository.update(user)

        entry.used = true
        passwordResetTokenRepository.update(entry)
        log.info("Password reset completed for user '{}'", user.username)
    }

    private fun generateRawToken(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun sha256Hex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val log = LoggerFactory.getLogger(PasswordResetService::class.java)
    }
}
