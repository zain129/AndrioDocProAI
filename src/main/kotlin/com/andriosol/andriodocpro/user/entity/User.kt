package com.andriosol.andriodocpro.user.entity

import com.andriosol.andriodocpro.common.entity.BaseEntity
import io.micronaut.data.annotation.MappedEntity

/**
 * A registered account. [passwordHash] is a bcrypt hash — the plaintext password is
 * never persisted or logged anywhere in this codebase. [role] is "USER" or "ADMIN";
 * admins can see and manage every document regardless of ownership/sharing.
 */
@MappedEntity("users")
class User(
    var username: String,
    var email: String,
    var passwordHash: String,
    var role: String = Roles.USER
) : BaseEntity()

object Roles {
    const val USER = "USER"
    const val ADMIN = "ADMIN"
}

val User.isAdmin: Boolean get() = role == Roles.ADMIN
