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
    val results: List<Movie> = emptyList(),          // Raw results from API
    val filteredResults: List<Movie> = emptyList(),  // Results after applying year & genre filters
    val displayResults: List<Movie> = emptyList(),   // Sliced results for UI (multiples of 21)
    val displayLimit: Int = 21,
    val allGenres: List<Genre> = emptyList(),        // Base genres from TMDB
    val displayGenres: List<Genre> = emptyList(),    // Genres actually available in current search results
    val selectedGenreId: Int? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
    val currentPage: Int = 1,
    val canLoadMore: Boolean = true,
    val activeYearFilter: String? = null
)

@OptIn(FlowPreview::class)
class SearchViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _queryFlow = MutableStateFlow("")
    private var defaultMoviesCache: List<Movie> = emptyList()

    init {
        loadGenres()
        loadDefaultMovies()
        observeQueryWithDebounce()
    }

    private fun observeQueryWithDebounce() {
        viewModelScope.launch {
            _queryFlow
                .debounce(500L)
                .filter { it.trim().length >= 2 || it.isBlank() }
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isNotBlank()) {
                        performSearch(query, page = 1)
                    } else {
                        // Revert to discover/default state
                        val genreId = _uiState.value.selectedGenreId
                        if (genreId != null) {
                            discoverByGenre(genreId, page = 1)
                        } else {
                            restoreDefaultMovies()
                        }
                    }
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        if (newQuery.isBlank()) {
            _uiState.update { it.copy(query = newQuery, errorMessage = null, selectedGenreId = null) }
        } else {
            _uiState.update { it.copy(query = newQuery, errorMessage = null) }
        }
        _queryFlow.value = newQuery
    }

    fun onGenreSelected(genreId: Int?) {
        val state = _uiState.value
        if (state.selectedGenreId == genreId) return
        _uiState.update { it.copy(selectedGenreId = genreId) }
        
        if (state.query.isBlank()) {
            if (genreId != null) {
                discoverByGenre(genreId, page = 1)
            } else {
                restoreDefaultMovies()
            }
        } else {
            // If currently searching, just apply local filter
            applyFilters(state.results)
        }
    }

    private fun performSearch(rawQuery: String, page: Int) {
        if (rawQuery.isBlank()) return
        
        // Extract year using regex (e.g., "Moana 2016")
        val yearRegex = Regex("""\b(19|20)\d{2}\b""")
        val match = yearRegex.find(rawQuery)
        val extractedYear = match?.value
        val cleanQuery = rawQuery.replace(yearRegex, "").trim()
        val queryToSearch = cleanQuery.ifBlank { rawQuery } // fallback if query is only a year

        viewModelScope.launch {
            if (page == 1) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, hasSearched = true, activeYearFilter = extractedYear) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
            }

            repository.searchMulti(queryToSearch, page).fold(
                onSuccess = { rawMovies ->
                    val newMovies = rawMovies.filter { it.posterUrl.isNotBlank() && it.title.isNotBlank() && it.releaseYear.isNotBlank() }
                    val isEnd = rawMovies.isEmpty() || rawMovies.size < 20 // Keep raw bounds for pagination check
                    _uiState.update { state ->
                        val combined = if (page == 1) newMovies else state.results + newMovies
                        val (filtered, dynamicGenres) = computeFiltersAndGenres(combined, state.selectedGenreId, state.activeYearFilter, state.allGenres)
                        val limit = if (page == 1) 21 else state.displayLimit
                        state.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            results = combined,
                            filteredResults = filtered,
                            displayResults = filtered.take(limit),
                            displayLimit = limit,
                            displayGenres = dynamicGenres,
                            currentPage = page,
                            canLoadMore = !isEnd
                        )
                    }
                    fetchNextPageIfNeeded()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = error.message ?: "Search failed."
                        )
                    }
                }
            )
        }
    }

    private fun discoverByGenre(genreId: Int, page: Int) {
        viewModelScope.launch {
            if (page == 1) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, hasSearched = false, activeYearFilter = null) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
            }

            repository.discoverMoviesByGenre(genreId, page).fold(
                onSuccess = { rawMovies ->
                    val newMovies = rawMovies.filter { it.posterUrl.isNotBlank() && it.title.isNotBlank() && it.releaseYear.isNotBlank() }
                    val isEnd = rawMovies.isEmpty() || rawMovies.size < 20
                    _uiState.update { state ->
                        val combined = if (page == 1) newMovies else state.results + newMovies
                        val limit = if (page == 1) 21 else state.displayLimit
                        state.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            results = combined,
                            filteredResults = combined, // no local filter needed
                            displayResults = combined.take(limit),
                            displayLimit = limit,
                            displayGenres = state.allGenres, // in discover mode, show all genres
                            currentPage = page,
                            canLoadMore = !isEnd
                        )
                    }
                    fetchNextPageIfNeeded()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, isLoadingMore = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore) return
        
        val newLimit = state.displayLimit + 21
        _uiState.update { it.copy(displayLimit = newLimit) }
        
        val needsMoreFromApi = state.filteredResults.size < newLimit
        if (needsMoreFromApi && state.canLoadMore) {
            val nextPage = state.currentPage + 1
            if (state.query.isNotBlank()) {
                performSearch(state.query, nextPage)
            } else if (state.selectedGenreId != null) {
                discoverByGenre(state.selectedGenreId, nextPage)
            } else {
                discoverPopular(nextPage)
            }
        } else {
            // We have enough cached results to show the next chunk (or no more API data)
            _uiState.update { it.copy(displayResults = it.filteredResults.take(newLimit)) }
        }
    }

    private fun fetchNextPageIfNeeded() {
        val state = _uiState.value
        if (state.filteredResults.size < state.displayLimit && state.canLoadMore) {
            val nextPage = state.currentPage + 1
            if (state.query.isNotBlank()) {
                performSearch(state.query, nextPage)
            } else if (state.selectedGenreId != null) {
                discoverByGenre(state.selectedGenreId, nextPage)
            } else {
                discoverPopular(nextPage)
            }
        }
    }

    private fun applyFilters(results: List<Movie>) {
        val state = _uiState.value
        val (filtered, dynamicGenres) = computeFiltersAndGenres(results, state.selectedGenreId, state.activeYearFilter, state.allGenres)
        _uiState.update {
            it.copy(
                filteredResults = filtered,
                displayResults = filtered.take(it.displayLimit),
                displayGenres = dynamicGenres
            )
        }
    }

    private fun computeFiltersAndGenres(
        rawResults: List<Movie>,
        selectedGenreId: Int?,
        activeYearFilter: String?,
        allGenres: List<Genre>
    ): Pair<List<Movie>, List<Genre>> {
        // 1. Filter by year
        val yearFiltered = if (activeYearFilter != null) {
            rawResults.filter { it.releaseDate.startsWith(activeYearFilter) }
        } else rawResults

        // 2. Compute dynamic genres available in the year-filtered results
        val availableGenreIds = yearFiltered.flatMap { it.genreIds }.toSet()
        val dynamicGenres = allGenres.filter { it.id in availableGenreIds }

        // 3. Filter by genre
        val fullyFiltered = if (selectedGenreId != null) {
            yearFiltered.filter { it.genreIds.contains(selectedGenreId) }
        } else yearFiltered

        return Pair(fullyFiltered, dynamicGenres)
    }

    private fun loadGenres() {
        viewModelScope.launch {
            repository.getGenres().fold(
                onSuccess = { genres ->
                    _uiState.update { it.copy(allGenres = genres, displayGenres = genres) }
                },
                onFailure = { }
            )
        }
    }

    private fun loadDefaultMovies() {
        discoverPopular(1)
    }

    private fun discoverPopular(page: Int) {
        viewModelScope.launch {
            if (page == 1) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null, hasSearched = false, activeYearFilter = null) }
            } else {
                _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
            }

            repository.getPopularMovies(page).fold(
                onSuccess = { rawMovies ->
                    val newMovies = rawMovies.filter { it.posterUrl.isNotBlank() && it.title.isNotBlank() && it.releaseYear.isNotBlank() }
                    val isEnd = rawMovies.isEmpty() || rawMovies.size < 20
                    _uiState.update { state ->
                        val combined = if (page == 1) newMovies else state.results + newMovies
                        if (page == 1) defaultMoviesCache = newMovies else defaultMoviesCache = combined
                        val limit = if (page == 1) 21 else state.displayLimit
                        state.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            results = combined,
                            filteredResults = combined,
                            displayResults = combined.take(limit),
                            displayLimit = limit,
                            displayGenres = state.allGenres,
                            currentPage = page,
                            canLoadMore = !isEnd
                        )
                    }
                    fetchNextPageIfNeeded()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, isLoadingMore = false, errorMessage = error.message) }
                }
            )
        }
    }

    private fun restoreDefaultMovies() {
        _uiState.update { state ->
            val limit = 21
            state.copy(
                isLoading = false,
                hasSearched = false,
                activeYearFilter = null,
                results = defaultMoviesCache,
                filteredResults = defaultMoviesCache,
                displayResults = defaultMoviesCache.take(limit),
                displayLimit = limit,
                displayGenres = state.allGenres,
                currentPage = 1,
                canLoadMore = true // default movies can load more now
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
