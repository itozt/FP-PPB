package com.example.moviecatalogue.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviecatalogue.domain.AuthRepository
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieRepository
import com.example.moviecatalogue.domain.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val session: UserSession? = null,
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = true,
    val recentlyDeletedMovie: Movie? = null
) {
    val isGuest: Boolean get() = session?.isGuest != false
}

class ProfileViewModel(
    private val repository: MovieRepository,
    authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(session = authRepository.session.value))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeWatchlist()
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            repository.getWatchlist()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .collect { movies ->
                    _uiState.update { it.copy(movies = movies, isLoading = false) }
                }
        }
    }

    fun removeMovie(movie: Movie) {
        viewModelScope.launch {
            _uiState.update { it.copy(recentlyDeletedMovie = movie) }
            repository.removeFromWatchlist(movie.id)
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            val movie = _uiState.value.recentlyDeletedMovie ?: return@launch
            repository.addToWatchlist(movie)
            _uiState.update { it.copy(recentlyDeletedMovie = null) }
        }
    }

    fun clearRecentlyDeleted() {
        _uiState.update { it.copy(recentlyDeletedMovie = null) }
    }

    class Factory(
        private val repository: MovieRepository,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProfileViewModel(repository, authRepository) as T
    }
}
