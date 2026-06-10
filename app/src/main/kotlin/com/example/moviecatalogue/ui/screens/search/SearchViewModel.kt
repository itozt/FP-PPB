package com.example.moviecatalogue.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviecatalogue.domain.Genre
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val defaultResults: List<Movie> = emptyList(),
    val results: List<Movie> = emptyList(),
    val filteredResults: List<Movie> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val selectedGenreId: Int? = null,
    val isLoading: Boolean = false,
    val isLoadingGenres: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false
)

@OptIn(FlowPreview::class)
class SearchViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Separate flow just for the query string to enable debounce
    private val _queryFlow = MutableStateFlow("")

    init {
        loadGenres()
        loadDefaultMovies()
        observeQueryWithDebounce()
    }

    private fun observeQueryWithDebounce() {
        viewModelScope.launch {
            _queryFlow
                .debounce(500L)          // 500ms debounce (HCI: responsiveness without spam)
                .filter { it.trim().length >= 2 } // Only search if ≥2 chars
                .distinctUntilChanged()
                .collectLatest { query ->
                    performSearch(query)
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery, errorMessage = null) }
        _queryFlow.value = newQuery

        if (newQuery.isBlank()) {
            _uiState.update {
                val defaultResults = it.defaultResults
                it.copy(
                    results = defaultResults,
                    filteredResults = applyGenreFilter(defaultResults, it.selectedGenreId),
                    isLoading = false,
                    hasSearched = false
                )
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, hasSearched = true) }
            repository.searchMovies(query).fold(
                onSuccess = { movies ->
                    _uiState.update { state ->
                        val filtered = applyGenreFilter(movies, state.selectedGenreId)
                        state.copy(
                            isLoading = false,
                            results = movies,
                            filteredResults = filtered
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Search failed. Check your connection."
                        )
                    }
                }
            )
        }
    }

    fun onGenreSelected(genreId: Int?) {
        _uiState.update { state ->
            val filtered = applyGenreFilter(state.results, genreId)
            state.copy(selectedGenreId = genreId, filteredResults = filtered)
        }
    }

    private fun applyGenreFilter(movies: List<Movie>, genreId: Int?): List<Movie> {
        return if (genreId == null) movies
        else movies.filter { it.genreIds.contains(genreId) }
    }

    private fun loadGenres() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingGenres = true) }
            repository.getGenres().fold(
                onSuccess = { genres ->
                    _uiState.update { it.copy(genres = genres, isLoadingGenres = false) }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingGenres = false) }
                }
            )
        }
    }

    private fun loadDefaultMovies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getPopularMovies().fold(
                onSuccess = { movies ->
                    _uiState.update { state ->
                        val filtered = applyGenreFilter(movies, state.selectedGenreId)
                        state.copy(
                            isLoading = false,
                            defaultResults = movies,
                            results = movies,
                            filteredResults = filtered
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load movies. Check your connection."
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(private val repository: MovieRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SearchViewModel(repository) as T
    }
}
