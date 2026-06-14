package com.example.moviecatalogue.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a locally-registered user account.
 *
 * Passwords are never stored in plain text: we keep a random per-user [salt]
 * and the SHA-256 hash of (salt + password) in [passwordHash].
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val createdAt: Long = System.currentTimeMillis()
)
