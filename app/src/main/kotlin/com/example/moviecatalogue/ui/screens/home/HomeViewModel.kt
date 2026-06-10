package com.example.moviecatalogue.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val trendingMovies: List<Movie> = emptyList(),
    val nowPlayingMovies: List<Movie> = emptyList(),
    val popularMovies: List<Movie> = emptyList(),
    val topRatedMovies: List<Movie> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Fetch all sections in parallel for snappy performance
            val trendingDeferred = async { repository.getTrendingMovies() }
            val nowPlayingDeferred = async { repository.getNowPlayingMovies() }
            val popularDeferred = async { repository.getPopularMovies() }
            val topRatedDeferred = async { repository.getTopRatedMovies() }

            val trending = trendingDeferred.await()
            val nowPlaying = nowPlayingDeferred.await()
            val popular = popularDeferred.await()
            val topRated = topRatedDeferred.await()

            val error = listOf(trending, nowPlaying, popular, topRated)
                .firstOrNull { it.isFailure }
                ?.exceptionOrNull()?.message

            _uiState.update {
                it.copy(
                    isLoading = false,
                    trendingMovies = trending.getOrDefault(emptyList()).take(5),
                    nowPlayingMovies = nowPlaying.getOrDefault(emptyList()),
                    popularMovies = popular.getOrDefault(emptyList()),
                    topRatedMovies = topRated.getOrDefault(emptyList()),
                    errorMessage = if (trending.isFailure && nowPlaying.isFailure) error else null
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(private val repository: MovieRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
