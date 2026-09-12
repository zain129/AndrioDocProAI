package com.andriosol.andriodocpro.user.service

import com.andriosol.andriodocpro.security.service.PasswordHasher
import com.andriosol.andriodocpro.user.dto.RegisterRequest
import com.andriosol.andriodocpro.user.entity.User
import com.andriosol.andriodocpro.user.repository.UserRepository
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class UserService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher
) {

    fun register(request: RegisterRequest): User {
        // Normalized so it matches how DocumentShare.email and forgot-password lookups compare.
        val email = request.email.trim().lowercase()

        if (userRepository.existsByUsername(request.username)) {
            log.warn("Registration rejected: username '{}' already taken", request.username)
            throw HttpStatusException(HttpStatus.CONFLICT, "Username already taken")
        }
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration rejected: email already registered")
            throw HttpStatusException(HttpStatus.CONFLICT, "Email already registered")
        }

        val saved = userRepository.save(
            User(
                username = request.username,
                email = email,
                passwordHash = passwordHasher.hash(request.password)
            )
        )
        log.info("Registered new user '{}'", saved.username)
        return saved
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserService::class.java)
    }
}
