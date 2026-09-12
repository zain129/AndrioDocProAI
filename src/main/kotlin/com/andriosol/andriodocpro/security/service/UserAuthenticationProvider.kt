package com.andriosol.andriodocpro.security.service

import com.andriosol.andriodocpro.user.repository.UserRepository
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.AuthenticationFailureReason
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.provider.HttpRequestAuthenticationProvider
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

/** Validates login credentials against the users table, comparing the bcrypt hash. */
@Singleton
class UserAuthenticationProvider<B>(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher
) : HttpRequestAuthenticationProvider<B> {

    override fun authenticate(
        requestContext: HttpRequest<B>?,
        authRequest: AuthenticationRequest<String, String>
    ): AuthenticationResponse {
        val user = userRepository.findByUsername(authRequest.identity).orElse(null)
        return if (user != null && passwordHasher.matches(authRequest.secret, user.passwordHash)) {
            log.info("Login succeeded for user '{}'", authRequest.identity)
            AuthenticationResponse.success(user.username)
        } else {
            log.warn("Login failed for user '{}'", authRequest.identity)
            AuthenticationResponse.failure(AuthenticationFailureReason.CREDENTIALS_DO_NOT_MATCH)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserAuthenticationProvider::class.java)
    }
}
