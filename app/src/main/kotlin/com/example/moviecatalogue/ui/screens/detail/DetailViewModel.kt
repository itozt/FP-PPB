package com.example.moviecatalogue.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moviecatalogue.domain.MediaType
import com.example.moviecatalogue.domain.MovieDetail
import com.example.moviecatalogue.domain.MovieRepository
import com.example.moviecatalogue.domain.TvEpisode
import com.example.moviecatalogue.domain.WatchProgress
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val movieDetail: MovieDetail? = null,
    val isInWatchlist: Boolean = false,
    val isWatchlistLoading: Boolean = false,
    val errorMessage: String? = null,
    val isTrailerPlaying: Boolean = false,
    // Streaming
    val isStreamingPlaying: Boolean = false,
    val mediaType: MediaType = MediaType.MOVIE,
    // TV Series
    val selectedSeason: Int = 1,
    val selectedEpisode: Int = 1,
    val episodes: List<TvEpisode> = emptyList(),
    val isLoadingEpisodes: Boolean = false,
    // Watch Progress
    val watchProgress: WatchProgress? = null,
    val resumeTimestamp: Int? = null
)

class DetailViewModel(
    private val movieId: Int,
    private val mediaType: MediaType,
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState(mediaType = mediaType))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
        observeWatchlistStatus()
        observeWatchProgress()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = when (mediaType) {
                MediaType.MOVIE -> repository.getMovieDetail(movieId)
                MediaType.TV -> repository.getTvDetail(movieId)
            }

            result.fold(
                onSuccess = { detail ->
                    _uiState.update { it.copy(isLoading = false, movieDetail = detail) }
                    // For TV, auto-load the first playable season's episodes
                    if (mediaType == MediaType.TV && detail.playableSeasons.isNotEmpty()) {
                        val firstSeason = detail.playableSeasons.first().seasonNumber
                        _uiState.update { it.copy(selectedSeason = firstSeason) }
                        loadEpisodes(firstSeason)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load details."
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

    private fun observeWatchProgress() {
        viewModelScope.launch {
            repository.getWatchProgressFlow(movieId, mediaType)
                .distinctUntilChanged()
                .collect { progress ->
                    _uiState.update {
                        it.copy(
                            watchProgress = progress
                        )
                    }
                }
        }
    }

    // ─── Watchlist ─────────────────────────────────────────────────────────────

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

    // ─── Trailer ──────────────────────────────────────────────────────────────

    fun playTrailer() {
        if (_uiState.value.movieDetail?.trailerKey.isNullOrBlank()) return
        _uiState.update { it.copy(isTrailerPlaying = true) }
    }

    fun closeTrailer() {
        _uiState.update { it.copy(isTrailerPlaying = false) }
    }

    // ─── Streaming ────────────────────────────────────────────────────────────

    fun playStream() {
        viewModelScope.launch {
            val state = _uiState.value
            val s = if (mediaType == MediaType.TV) state.selectedSeason else null
            val e = if (mediaType == MediaType.TV) state.selectedEpisode else null
            val specificProgress = repository.getWatchProgress(movieId, mediaType, s, e)
            _uiState.update {
                it.copy(
                    resumeTimestamp = specificProgress?.currentTime?.toInt(),
                    isStreamingPlaying = true
                )
            }
        }
    }

    fun playEpisode(season: Int, episode: Int) {
        viewModelScope.launch {
            val specificProgress = repository.getWatchProgress(movieId, mediaType, season, episode)
            _uiState.update {
                it.copy(
                    selectedSeason = season,
                    selectedEpisode = episode,
                    resumeTimestamp = specificProgress?.currentTime?.toInt(),
                    isStreamingPlaying = true
                )
            }
        }
    }

    fun closeStream() {
        _uiState.update { it.copy(isStreamingPlaying = false) }
    }

    fun onStreamProgress(currentTime: Double, duration: Double, progress: Double) {
        // Throttle: only save progress every meaningful chunk (> 5 seconds change)
        val state = _uiState.value
        val movie = state.movieDetail?.movie
        if (movie == null) return

        viewModelScope.launch {
            repository.saveWatchProgress(
                WatchProgress(
                    contentId = movieId,
                    mediaType = mediaType,
                    currentTime = currentTime,
                    duration = duration,
                    progress = progress,
                    title = movie.displayTitle,
                    posterUrl = movie.posterUrl,
                    backdropUrl = movie.backdropUrl.ifEmpty { movie.posterUrl },
                    season = if (mediaType == MediaType.TV) state.selectedSeason else null,
                    episode = if (mediaType == MediaType.TV) state.selectedEpisode else null
                )
            )
        }
    }

    // ─── TV Season / Episode ──────────────────────────────────────────────────

    fun selectSeason(seasonNumber: Int) {
        _uiState.update { it.copy(selectedSeason = seasonNumber) }
        loadEpisodes(seasonNumber)
    }

    private fun loadEpisodes(seasonNumber: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingEpisodes = true) }
            repository.getTvSeasonEpisodes(movieId, seasonNumber).fold(
                onSuccess = { episodes ->
                    _uiState.update {
                        it.copy(isLoadingEpisodes = false, episodes = episodes)
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingEpisodes = false, episodes = emptyList()) }
                }
            )
        }
    }

    // ─── Misc ─────────────────────────────────────────────────────────────────

    fun retryLoad() = loadDetail()

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(
        private val movieId: Int,
        private val mediaType: MediaType,
        private val repository: MovieRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetailViewModel(movieId, mediaType, repository) as T
    }
}
