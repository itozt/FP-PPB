package com.example.moviecatalogue.data.repository

import com.example.moviecatalogue.data.auth.PasswordHasher
import com.example.moviecatalogue.data.auth.SessionManager
import com.example.moviecatalogue.data.local.UserDao
import com.example.moviecatalogue.data.local.UserEntity
import com.example.moviecatalogue.domain.AuthRepository
import com.example.moviecatalogue.domain.UserSession
import kotlinx.coroutines.flow.StateFlow

class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : AuthRepository {

    override val session: StateFlow<UserSession?> = sessionManager.session

    override suspend fun register(name: String, email: String, password: String): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        if (userDao.countByEmail(cleanEmail) > 0) {
            return Result.failure(Exception("Email sudah terdaftar."))
        }
        val salt = PasswordHasher.generateSalt()
        val entity = UserEntity(
            name = name.trim(),
            email = cleanEmail,
            passwordHash = PasswordHasher.hash(password, salt),
            salt = salt
        )
        return try {
            val newId = userDao.insert(entity).toInt()
            sessionManager.save(
                UserSession(userId = newId, name = entity.name, email = cleanEmail, isGuest = false)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal mendaftar. Coba lagi."))
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        val user = userDao.getByEmail(cleanEmail)
            ?: return Result.failure(Exception("Email tidak ditemukan."))
        if (!PasswordHasher.verify(password, user.salt, user.passwordHash)) {
            return Result.failure(Exception("Password salah."))
        }
        sessionManager.save(
            UserSession(userId = user.id, name = user.name, email = user.email, isGuest = false)
        )
        return Result.success(Unit)
    }

    override fun loginAsGuest() {
        sessionManager.save(
            UserSession(
                userId = UserSession.GUEST_ID,
                name = "Guest",
                email = "",
                isGuest = true
            )
        )
    }

    override fun logout() {
        sessionManager.clear()
    }
}
