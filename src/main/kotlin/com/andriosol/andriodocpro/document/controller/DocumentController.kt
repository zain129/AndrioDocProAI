package com.andriosol.andriodocpro.document.controller

import com.andriosol.andriodocpro.document.dto.ChunkView
import com.andriosol.andriodocpro.document.dto.DocumentResponse
import com.andriosol.andriodocpro.document.dto.ShareRequest
import com.andriosol.andriodocpro.document.dto.ShareResponse
import com.andriosol.andriodocpro.document.dto.ShareView
import com.andriosol.andriodocpro.document.service.DocumentService
import com.andriosol.andriodocpro.document.service.DocumentShareService
import com.andriosol.andriodocpro.rag.dto.AnswerResponse
import com.andriosol.andriodocpro.rag.dto.AskRequest
import com.andriosol.andriodocpro.rag.service.RagService
import com.andriosol.andriodocpro.security.service.CurrentUserService
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import jakarta.validation.Valid
import java.util.UUID

@Controller("/documents")
@Validated
@Secured(SecurityRule.IS_AUTHENTICATED)
class DocumentController(
    private val documentService: DocumentService,
    private val documentShareService: DocumentShareService,
    private val ragService: RagService,
    private val currentUserService: CurrentUserService
) {

    /** Upload a PDF/DOC/DOCX; its text is extracted, chunked, embedded and stored. You become its owner. */
    @Post(consumes = [MediaType.MULTIPART_FORM_DATA])
    @Status(HttpStatus.CREATED)
    fun upload(file: CompletedFileUpload, authentication: Authentication): DocumentResponse =
        documentService.store(file, currentUserService.resolve(authentication))

    /** Documents you own, documents shared with you, or (for admins) every document. */
    @Get
    fun list(authentication: Authentication): List<DocumentResponse> =
        documentService.list(currentUserService.resolve(authentication))

    @Get("/{id}")
    fun get(id: UUID, authentication: Authentication): DocumentResponse =
        documentService.get(id, currentUserService.resolve(authentication))

    @Get("/{id}/chunks")
    fun chunks(id: UUID, authentication: Authentication): List<ChunkView> =
        documentService.chunks(id, currentUserService.resolve(authentication))

    /** Ask a question answered only from this specific document. */
    @Post("/{id}/ask")
    fun ask(id: UUID, @Body @Valid request: AskRequest, authentication: Authentication): AnswerResponse =
        ragService.answerForDocument(id, request.question, currentUserService.resolve(authentication))

    /** Owner/admin only. */
    @Delete("/{id}")
    @Status(HttpStatus.NO_CONTENT)
    fun delete(id: UUID, authentication: Authentication) =
        documentService.delete(id, currentUserService.resolve(authentication))

    /**
     * Grants an email address read access to this document. If no account exists for that
     * email yet, an invitation is sent instead — access still takes effect the moment they
     * register with that address. Owner/admin only.
     */
    @Post("/{id}/share")
    fun share(id: UUID, @Body @Valid request: ShareRequest, authentication: Authentication): ShareResponse =
        documentShareService.share(id, request.email, currentUserService.resolve(authentication))

    /** Owner/admin only. */
    @Get("/{id}/shares")
    fun shares(id: UUID, authentication: Authentication): List<ShareView> =
        documentShareService.listShares(id, currentUserService.resolve(authentication))

    /** Owner/admin only. */
    @Delete("/{id}/shares/{email}")
    @Status(HttpStatus.NO_CONTENT)
    fun revokeShare(id: UUID, email: String, authentication: Authentication) =
        documentShareService.revoke(id, email, currentUserService.resolve(authentication))
}
