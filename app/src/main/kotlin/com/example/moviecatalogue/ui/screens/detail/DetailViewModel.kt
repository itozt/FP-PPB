package com.example.moviecatalogue.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviecatalogue.domain.MovieDetail
import com.example.moviecatalogue.domain.MovieRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val movieDetail: MovieDetail? = null,
    val isInWatchlist: Boolean = false,
    val isWatchlistLoading: Boolean = false,
    val errorMessage: String? = null,
    val isTrailerPlaying: Boolean = false
)

class DetailViewModel(
    private val movieId: Int,
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadMovieDetail()
        observeWatchlistStatus()
    }

    private fun loadMovieDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getMovieDetail(movieId).fold(
                onSuccess = { detail ->
                    _uiState.update { it.copy(isLoading = false, movieDetail = detail) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load movie details."
                        )
                    }
                }
            )
        }
    }

    private fun observeWatchlistStatus() {
        viewModelScope.launch {
            repository.isInWatchlistFlow(movieId)
                .distinctUntilChanged()
                .collect { inWatchlist ->
                    _uiState.update { it.copy(isInWatchlist = inWatchlist) }
                }
        }
    }

    fun toggleWatchlist() {
        val state = _uiState.value
        val movie = state.movieDetail?.movie ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isWatchlistLoading = true) }
            if (state.isInWatchlist) {
                repository.removeFromWatchlist(movieId)
            } else {
                repository.addToWatchlist(movie)
            }
            _uiState.update { it.copy(isWatchlistLoading = false) }
        }
    }

    fun playTrailer() {
        if (_uiState.value.movieDetail?.trailerKey.isNullOrBlank()) return
        _uiState.update { it.copy(isTrailerPlaying = true) }
    }

    fun closeTrailer() {
        _uiState.update { it.copy(isTrailerPlaying = false) }
    }

    fun retryLoad() = loadMovieDetail()

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(
        private val movieId: Int,
        private val repository: MovieRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetailViewModel(movieId, repository) as T
    }
}
