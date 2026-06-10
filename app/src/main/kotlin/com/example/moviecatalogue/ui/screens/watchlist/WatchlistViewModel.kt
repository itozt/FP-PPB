package com.example.moviecatalogue.ui.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WatchlistUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = true,
    val recentlyDeletedMovie: Movie? = null
)

class WatchlistViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

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
            // Save for potential undo (HCI: error recovery via snackbar undo)
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

    class Factory(private val repository: MovieRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WatchlistViewModel(repository) as T
    }
}
