package com.example.moviecatalogue.domain

/**
 * The currently signed-in user. [isGuest] sessions have no account row
 * (their [userId] is [GUEST_ID]) and are not persisted beyond convenience.
 */
data class UserSession(
    val userId: Int,
    val name: String,
    val email: String,
    val isGuest: Boolean
) {
    companion object {
        const val GUEST_ID = -1
    }
}
