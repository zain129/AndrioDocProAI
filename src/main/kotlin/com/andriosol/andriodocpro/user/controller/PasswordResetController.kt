package com.andriosol.andriodocpro.user.controller

import com.andriosol.andriodocpro.user.dto.ForgotPasswordRequest
import com.andriosol.andriodocpro.user.dto.MessageResponse
import com.andriosol.andriodocpro.user.dto.ResetPasswordRequest
import com.andriosol.andriodocpro.user.service.PasswordResetService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import jakarta.validation.Valid

@Controller("/password")
@Validated
@Secured(SecurityRule.IS_ANONYMOUS)
class PasswordResetController(private val passwordResetService: PasswordResetService) {

    /**
     * Always responds with the same generic message, whether or not the email is
     * registered, so this endpoint cannot be used to enumerate accounts.
     */
    @Post("/forgot")
    fun forgotPassword(@Body @Valid request: ForgotPasswordRequest): MessageResponse {
        passwordResetService.forgotPassword(request.email)
        return MessageResponse("If that email is registered, a password reset link has been sent.")
    }

    @Post("/reset")
    fun resetPassword(@Body @Valid request: ResetPasswordRequest): MessageResponse {
        passwordResetService.resetPassword(request.token, request.newPassword)
        return MessageResponse("Password updated. You can now sign in.")
    }
}
