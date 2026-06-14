package com.example.moviecatalogue.data.auth

import android.content.Context
import com.example.moviecatalogue.domain.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists the active [UserSession] in SharedPreferences and exposes it as a
 * reactive [StateFlow] so the UI can react to login/logout.
 */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("auth_session", Context.MODE_PRIVATE)

    private val _session = MutableStateFlow(load())
    val session: StateFlow<UserSession?> = _session.asStateFlow()

    fun save(session: UserSession) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putInt(KEY_USER_ID, session.userId)
            .putString(KEY_NAME, session.name)
            .putString(KEY_EMAIL, session.email)
            .putBoolean(KEY_GUEST, session.isGuest)
            .apply()
        _session.value = session
    }

    fun clear() {
        prefs.edit().clear().apply()
        _session.value = null
    }

    private fun load(): UserSession? {
        if (!prefs.getBoolean(KEY_LOGGED_IN, false)) return null
        return UserSession(
            userId = prefs.getInt(KEY_USER_ID, UserSession.GUEST_ID),
            name = prefs.getString(KEY_NAME, "") ?: "",
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            isGuest = prefs.getBoolean(KEY_GUEST, false)
        )
    }

    private companion object {
        const val KEY_LOGGED_IN = "logged_in"
        const val KEY_USER_ID = "user_id"
        const val KEY_NAME = "name"
        const val KEY_EMAIL = "email"
        const val KEY_GUEST = "is_guest"
    }
}
