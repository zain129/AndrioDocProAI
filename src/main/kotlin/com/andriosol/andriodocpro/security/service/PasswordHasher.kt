package com.andriosol.andriodocpro.security.service

import jakarta.inject.Singleton
import org.mindrot.jbcrypt.BCrypt

/**
 * One-way password hashing. Plaintext passwords are never stored or logged anywhere in
 * this codebase — only the bcrypt output of [hash] is persisted. Because bcrypt salts
 * automatically, hashing the same password twice yields two different (still valid) hashes.
 */
@Singleton
class PasswordHasher {

    fun hash(rawPassword: String): String = BCrypt.hashpw(rawPassword, BCrypt.gensalt())

    fun matches(rawPassword: String, hash: String): Boolean =
        try {
            BCrypt.checkpw(rawPassword, hash)
        } catch (e: IllegalArgumentException) {
            // malformed hash (e.g. corrupted data) — treat as no match rather than throwing
            false
        }
}
