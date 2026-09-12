package com.andriosol.andriodocpro.user.controller

import com.andriosol.andriodocpro.user.dto.RegisterRequest
import com.andriosol.andriodocpro.user.dto.RegisterResponse
import com.andriosol.andriodocpro.user.dto.toResponse
import com.andriosol.andriodocpro.user.service.UserService
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import jakarta.validation.Valid

@Controller("/register")
@Validated
@Secured(SecurityRule.IS_ANONYMOUS)
class RegistrationController(private val userService: UserService) {

    @Post
    @Status(HttpStatus.CREATED)
    fun register(@Body @Valid request: RegisterRequest): RegisterResponse =
        userService.register(request).toResponse()
}
