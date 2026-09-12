package com.andriosol.andriodocpro.document.service

import com.andriosol.andriodocpro.document.dto.ShareResponse
import com.andriosol.andriodocpro.document.dto.ShareView
import com.andriosol.andriodocpro.document.entity.DocumentShare
import com.andriosol.andriodocpro.document.repository.DocumentShareRepository
import com.andriosol.andriodocpro.notification.EmailSender
import com.andriosol.andriodocpro.user.entity.User
import com.andriosol.andriodocpro.user.repository.UserRepository
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class DocumentShareService(
    private val documentService: DocumentService,
    private val documentAccessService: DocumentAccessService,
    private val documentShareRepository: DocumentShareRepository,
    private val userRepository: UserRepository,
    private val emailSender: EmailSender,
    @Value("\${app.frontend-base-url:http://localhost:5173}") private val frontendBaseUrl: String
) {

    /**
     * Grants [rawEmail] access to [documentId]. If that address already has an account,
     * access is immediate. Otherwise an invitation is emailed — access still takes effect
     * the moment they register with that address, since sharing is keyed by email.
     */
    fun share(documentId: UUID, rawEmail: String, currentUser: User): ShareResponse {
        val email = rawEmail.trim().lowercase()
        val document = documentService.findOrThrow(documentId)
        documentAccessService.requireManageAccess(document, currentUser)

        val alreadyShared = documentShareRepository.existsByDocumentIdAndEmail(documentId, email)
        if (!alreadyShared) {
            documentShareRepository.save(DocumentShare(documentId, email, currentUser.id!!))
        }

        val registered = userRepository.existsByEmail(email)
        if (!registered) {
            val link = "$frontendBaseUrl/register?email=$email"
            emailSender.send(
                email,
                "You've been invited to a document on AndrioDocPro AI",
                "${currentUser.username} shared \"${document.filename}\" with you. " +
                    "Create an account with this email address to view it: $link"
            )
        }

        log.info(
            "Document {} shared with {} by '{}' ({})",
            documentId, email, currentUser.username, if (registered) "existing user" else "invited"
        )

        return ShareResponse(
            email = email,
            alreadyRegistered = registered,
            message = if (registered) "$email can now access this document."
            else "$email doesn't have an account yet — an invitation was sent."
        )
    }

    fun listShares(documentId: UUID, currentUser: User): List<ShareView> {
        val document = documentService.findOrThrow(documentId)
        documentAccessService.requireManageAccess(document, currentUser)
        return documentShareRepository.findByDocumentIdOrderByCreatedAt(documentId)
            .map { ShareView(it.email, it.createdAt) }
    }

    fun revoke(documentId: UUID, rawEmail: String, currentUser: User) {
        val email = rawEmail.trim().lowercase()
        val document = documentService.findOrThrow(documentId)
        documentAccessService.requireManageAccess(document, currentUser)

        val deleted = documentShareRepository.deleteByDocumentIdAndEmail(documentId, email)
        if (deleted == 0L) {
            throw HttpStatusException(HttpStatus.NOT_FOUND, "$email does not have access to this document")
        }
        log.info("Revoked {}'s access to document {} (by '{}')", email, documentId, currentUser.username)
    }

    companion object {
        private val log = LoggerFactory.getLogger(DocumentShareService::class.java)
    }
}
