package com.example.moviecatalogue.data.repository

import com.example.moviecatalogue.data.local.MovieDao
import com.example.moviecatalogue.data.local.WatchProgressDao
import com.example.moviecatalogue.data.local.toEntity
import com.example.moviecatalogue.data.remote.ApiService
import com.example.moviecatalogue.domain.AuthRepository
import com.example.moviecatalogue.domain.Genre
import com.example.moviecatalogue.domain.MediaType
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieDetail
import com.example.moviecatalogue.domain.MovieRepository
import com.example.moviecatalogue.domain.MovieVideo
import com.example.moviecatalogue.domain.TvEpisode
import com.example.moviecatalogue.domain.UserSession
import com.example.moviecatalogue.domain.WatchProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Concrete implementation of MovieRepository.
 * Bridges the remote API and local Room database,
 * acting as the Single Source of Truth.
 */
class MovieRepositoryImpl(
    private val apiService: ApiService,
    private val movieDao: MovieDao,
    private val watchProgressDao: WatchProgressDao,
    private val authRepository: AuthRepository
) : MovieRepository {

    /** The id of the signed-in account; GUEST_ID when browsing as a guest. */
    private val currentUserId: Int
        get() = authRepository.session.value?.userId ?: UserSession.GUEST_ID

    private val isGuest: Boolean
        get() = currentUserId == UserSession.GUEST_ID

    // ─── Remote Operations ─────────────────────────────────────────────────────

    override suspend fun getTrendingMovies(): Result<List<Movie>> = safeApiCall {
        apiService.getTrendingAll().results.mapNotNull { it.toDomain() }.take(10)
    }

    override suspend fun getNowPlayingMovies(): Result<List<Movie>> = safeApiCall {
        apiService.getNowPlayingMovies().results.map { it.toDomain() }
    }

    override suspend fun getPopularMovies(page: Int): Result<List<Movie>> = safeApiCall {
        apiService.getPopularMovies(page).results.map { it.toDomain() }
    }

    override suspend fun getTopRatedMovies(): Result<List<Movie>> = safeApiCall {
        apiService.getTopRatedMovies().results.map { it.toDomain() }
    }

    // ─── TV Series Lists ──────────────────────────────────────────────────────

    override suspend fun getPopularTvShows(): Result<List<Movie>> = safeApiCall {
        apiService.getPopularTvShows().results.map { it.toDomain() }
    }

    override suspend fun getTopRatedTvShows(): Result<List<Movie>> = safeApiCall {
        apiService.getTopRatedTvShows().results.map { it.toDomain() }
    }

    // ─── Search & Discover ────────────────────────────────────────────────────

    override suspend fun searchMulti(query: String, page: Int): Result<List<Movie>> = safeApiCall {
        apiService.searchMulti(query, page).results.mapNotNull { it.toDomain() }
    }

    override suspend fun discoverMoviesByGenre(genreId: Int, page: Int): Result<List<Movie>> = safeApiCall {
        apiService.discoverMovies(genreId, page).results.map { it.toDomain() }
    }

    // ─── Movie Detail ─────────────────────────────────────────────────────────

    override suspend fun getMovieDetail(movieId: Int): Result<MovieDetail> = safeApiCall {
        val movieDetailDto = apiService.getMovieDetail(movieId)
        val videos = try {
            apiService.getMovieVideos(movieId).results.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
        MovieDetail(
            movie = movieDetailDto.toDomain(),
            videos = videos,
            runtime = movieDetailDto.runtime,
            tagline = movieDetailDto.tagline,
            status = movieDetailDto.status
        )
    }

    override suspend fun getMovieVideos(movieId: Int): Result<List<MovieVideo>> = safeApiCall {
        apiService.getMovieVideos(movieId).results.map { it.toDomain() }
    }

    // ─── TV Series Detail ─────────────────────────────────────────────────────

    override suspend fun getTvDetail(tvId: Int): Result<MovieDetail> = safeApiCall {
        val tvDetailDto = apiService.getTvDetail(tvId)
        val videos = try {
            apiService.getTvVideos(tvId).results.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
        MovieDetail(
            movie = tvDetailDto.toDomain(),
            videos = videos,
            runtime = tvDetailDto.episodeRunTime.firstOrNull() ?: 0,
            tagline = tvDetailDto.tagline,
            status = tvDetailDto.status,
            numberOfSeasons = tvDetailDto.numberOfSeasons,
            seasons = tvDetailDto.seasons.map { it.toDomain() }
        )
    }

    override suspend fun getTvSeasonEpisodes(tvId: Int, seasonNumber: Int): Result<List<TvEpisode>> =
        safeApiCall {
            apiService.getTvSeasonDetail(tvId, seasonNumber).episodes.map { it.toDomain() }
        }

    // ─── Genres ───────────────────────────────────────────────────────────────

    override suspend fun getGenres(): Result<List<Genre>> = safeApiCall {
        apiService.getGenres().genres.map { it.toDomain() }
    }

    // ─── Local (Room) Operations — Watchlist ──────────────────────────────────

    override fun getWatchlist(): Flow<List<Movie>> {
        if (isGuest) return flowOf(emptyList())
        return movieDao.getAllWatchlistMovies(currentUserId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addToWatchlist(movie: Movie) {
        if (isGuest) return  // guests cannot persist a watchlist
        movieDao.insertMovie(movie.toEntity(currentUserId))
    }

    override suspend fun removeFromWatchlist(movieId: Int) {
        if (isGuest) return
        movieDao.deleteMovieById(movieId, currentUserId)
    }

    override suspend fun isInWatchlist(movieId: Int): Boolean =
        !isGuest && movieDao.isInWatchlist(movieId, currentUserId)

    override fun isInWatchlistFlow(movieId: Int): Flow<Boolean> =
        if (isGuest) flowOf(false) else movieDao.isInWatchlistFlow(movieId, currentUserId)

    // ─── Local (Room) Operations — Watch Progress ─────────────────────────────

    override suspend fun saveWatchProgress(progress: WatchProgress) {
        watchProgressDao.upsertProgress(progress.toEntity(currentUserId))
    }

    override suspend fun getWatchProgress(
        contentId: Int,
        mediaType: MediaType,
        season: Int?,
        episode: Int?
    ): WatchProgress? {
        return watchProgressDao.getProgress(
            contentId = contentId,
            userId = currentUserId,
            mediaType = mediaType.value,
            season = season ?: 0,
            episode = episode ?: 0
        )?.toDomain()
    }

    override fun getWatchProgressFlow(contentId: Int, mediaType: MediaType): Flow<WatchProgress?> {
        return watchProgressDao.getLatestProgressFlow(
            contentId = contentId,
            userId = currentUserId,
            mediaType = mediaType.value
        ).map { it?.toDomain() }
    }

    override fun getContinueWatching(): Flow<List<WatchProgress>> {
        return watchProgressDao.getContinueWatching(currentUserId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // ─── Helper ────────────────────────────────────────────────────────────────

    private suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
        return try {
            Result.success(call())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
