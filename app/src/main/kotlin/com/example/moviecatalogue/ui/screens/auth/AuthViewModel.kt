package com.example.moviecatalogue.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviecatalogue.domain.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (!validate(email = email, password = password)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email, password).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, success = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun register(name: String, email: String, password: String, confirm: String) {
        when {
            name.isBlank() -> { setError("Nama tidak boleh kosong."); return }
            !validate(email = email, password = password) -> return
            password != confirm -> { setError("Konfirmasi password tidak cocok."); return }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.register(name, email, password).fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, success = true) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun loginAsGuest() {
        authRepository.loginAsGuest()
        _uiState.update { it.copy(success = true) }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun validate(email: String, password: String): Boolean {
        val emailOk = android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        return when {
            !emailOk -> { setError("Format email tidak valid."); false }
            password.length < 6 -> { setError("Password minimal 6 karakter."); false }
            else -> true
        }
    }

    private fun setError(message: String) = _uiState.update { it.copy(error = message) }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(authRepository) as T
    }
}
