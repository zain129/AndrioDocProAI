package com.andriosol.andriodocpro.security.service

import com.andriosol.andriodocpro.user.entity.User
import com.andriosol.andriodocpro.user.repository.UserRepository
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.security.authentication.Authentication
import jakarta.inject.Singleton

/** Resolves the full [User] row behind a request's JWT (identified by username). */
@Singleton
class CurrentUserService(private val userRepository: UserRepository) {

    fun resolve(authentication: Authentication): User =
        userRepository.findByUsername(authentication.name).orElseThrow {
            HttpStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user no longer exists")
        }
}
