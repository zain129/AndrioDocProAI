package com.andriosol.andriodocpro.security.service

import com.andriosol.andriodocpro.security.entity.RefreshTokenEntity
import com.andriosol.andriodocpro.security.repository.RefreshTokenRepository
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.errors.IssuingAnAccessTokenErrorCode
import io.micronaut.security.errors.OauthErrorResponseException
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent
import io.micronaut.security.token.refresh.RefreshTokenPersistence
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Flux

/**
 * Stores refresh tokens in Postgres so they can be validated and revoked.
 * Access tokens remain stateless JWTs; only the long-lived refresh tokens
 * touch the database (once per token issue/refresh, not per request).
 */
@Singleton
class DbRefreshTokenPersistence(
    private val repository: RefreshTokenRepository
) : RefreshTokenPersistence {

    override fun persistToken(event: RefreshTokenGeneratedEvent?) {
        val username = event?.authentication?.name
        val token = event?.refreshToken
        if (username != null && token != null) {
            repository.save(RefreshTokenEntity(username, token))
            log.debug("Persisted refresh token for user '{}'", username)
        }
    }

    override fun getAuthentication(refreshToken: String): Publisher<Authentication> =
        Flux.create({ emitter ->
            val stored = repository.findByRefreshToken(refreshToken).orElse(null)
            when {
                stored == null -> {
                    log.warn("Refresh attempted with unknown token")
                    emitter.error(OauthErrorResponseException(
                        IssuingAnAccessTokenErrorCode.INVALID_GRANT, "refresh token not found", null))
                }
                stored.revoked -> {
                    log.warn("Refresh attempted with revoked token for user '{}'", stored.username)
                    emitter.error(OauthErrorResponseException(
                        IssuingAnAccessTokenErrorCode.INVALID_GRANT, "refresh token revoked", null))
                }
                else -> {
                    log.debug("Refresh token accepted for user '{}'", stored.username)
                    emitter.next(Authentication.build(stored.username))
                    emitter.complete()
                }
            }
        }, FluxSink.OverflowStrategy.ERROR)

    companion object {
        private val log = LoggerFactory.getLogger(DbRefreshTokenPersistence::class.java)
    }
}
