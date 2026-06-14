package com.example.moviecatalogue.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * Local (Room-backed) authentication. No network/cloud — accounts live on-device.
 */
interface AuthRepository {

    /** The active session, or null when signed out. */
    val session: StateFlow<UserSession?>

    suspend fun register(name: String, email: String, password: String): Result<Unit>

    suspend fun login(email: String, password: String): Result<Unit>

    fun loginAsGuest()

    fun logout()
}
