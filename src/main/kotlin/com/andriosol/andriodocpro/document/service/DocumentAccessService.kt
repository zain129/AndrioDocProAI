package com.andriosol.andriodocpro.document.service

import com.andriosol.andriodocpro.document.entity.Document
import com.andriosol.andriodocpro.document.repository.DocumentRepository
import com.andriosol.andriodocpro.document.repository.DocumentShareRepository
import com.andriosol.andriodocpro.user.entity.User
import com.andriosol.andriodocpro.user.entity.isAdmin
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import jakarta.inject.Singleton
import java.util.UUID

/**
 * A document is visible to its owner, anyone it's been shared with (by email), and admins.
 * Only the owner or an admin may delete it or manage its shares.
 */
@Singleton
class DocumentAccessService(
    private val documentRepository: DocumentRepository,
    private val documentShareRepository: DocumentShareRepository
) {

    /** null means unrestricted (admin) — every document is accessible. */
    fun accessibleDocumentIds(user: User): Set<UUID>? {
        if (user.isAdmin) return null
        val owned = documentRepository.findByOwnerId(user.id!!).mapNotNull { it.id }
        val shared = documentShareRepository.findByEmail(user.email).map { it.documentId }
        return (owned + shared).toSet()
    }

    /** Throws 404 (never 403) so a document you have no access to appears not to exist. */
    fun requireViewAccess(document: Document, user: User) {
        if (canView(document, user)) return
        throw HttpStatusException(HttpStatus.NOT_FOUND, "Document ${document.id} not found")
    }

    /**
     * Throws 404 if you can't even view the document, or 403 if you can view it (it was
     * shared with you) but aren't the owner or an admin — safe to be specific here since
     * you already know the document exists.
     */
    fun requireManageAccess(document: Document, user: User) {
        requireViewAccess(document, user)
        if (user.isAdmin || document.ownerId == user.id) return
        throw HttpStatusException(HttpStatus.FORBIDDEN, "Only the owner or an admin can do this")
    }

    private fun canView(document: Document, user: User): Boolean =
        user.isAdmin ||
            document.ownerId == user.id ||
            documentShareRepository.existsByDocumentIdAndEmail(document.id!!, user.email)
}
