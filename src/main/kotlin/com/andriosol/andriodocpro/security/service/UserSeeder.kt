package com.andriosol.andriodocpro.security.service

import com.andriosol.andriodocpro.user.entity.User
import com.andriosol.andriodocpro.user.repository.UserRepository
import io.micronaut.context.annotation.Value
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

/**
 * Ensures a default admin account exists on startup, seeded from app.security.username/password
 * (APP_USERNAME / APP_PASSWORD). Idempotent: does nothing once that username is registered,
 * so it never overwrites a password the admin (or the registration flow) has since changed.
 */
@Singleton
class UserSeeder(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    @Value("\${app.security.username}") private val username: String,
    @Value("\${app.security.password}") private val password: String,
    @Value("\${app.security.admin-email:admin@andriodocpro.local}") private val email: String
) {

    @EventListener
    fun onStartup(event: StartupEvent) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.save(User(username, email, passwordHasher.hash(password)))
            log.info("Seeded default account for user '{}'", username)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserSeeder::class.java)
    }
}
