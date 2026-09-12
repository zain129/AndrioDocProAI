package com.andriosol.andriodocpro.rag.controller

import com.andriosol.andriodocpro.rag.dto.AnswerResponse
import com.andriosol.andriodocpro.rag.dto.AskRequest
import com.andriosol.andriodocpro.rag.service.RagService
import com.andriosol.andriodocpro.security.service.CurrentUserService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import jakarta.validation.Valid

/** Ask a question answered from every document you own or have been shared (all documents, for admins). */
@Controller("/search")
@Validated
@Secured(SecurityRule.IS_AUTHENTICATED)
class SearchController(
    private val ragService: RagService,
    private val currentUserService: CurrentUserService
) {

    @Post
    fun search(@Body @Valid request: AskRequest, authentication: Authentication): AnswerResponse =
        ragService.answerAcrossDocuments(request.question, currentUserService.resolve(authentication))
}
