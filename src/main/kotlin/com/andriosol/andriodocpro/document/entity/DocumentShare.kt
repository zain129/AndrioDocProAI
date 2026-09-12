package com.andriosol.andriodocpro.document.entity

import com.andriosol.andriodocpro.common.entity.BaseEntity
import io.micronaut.data.annotation.MappedEntity
import java.util.UUID

/**
 * Grants [email] read access to [documentId] (view + ask, not delete/share). Keyed by email
 * rather than a user id so that sharing with someone who hasn't registered yet still takes
 * effect the instant they sign up with that address — no backfill step required.
 */
@MappedEntity("document_shares")
class DocumentShare(
    var documentId: UUID,
    var email: String,
    var sharedByUserId: UUID
) : BaseEntity()
