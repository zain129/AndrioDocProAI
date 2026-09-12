package com.andriosol.andriodocpro.security.entity

import com.andriosol.andriodocpro.common.entity.BaseEntity
import io.micronaut.data.annotation.MappedEntity

/**
 * A persisted refresh token. Storing refresh tokens server-side is what makes them
 * revocable: flipping [revoked] (or deleting the row) immediately invalidates the
 * token even though the access tokens themselves stay stateless.
 */
@MappedEntity("refresh_tokens")
class RefreshTokenEntity(
    var username: String,
    var refreshToken: String,
    var revoked: Boolean = false
) : BaseEntity()
