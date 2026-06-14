package com.example.moviecatalogue.data.repository

import com.example.moviecatalogue.data.local.MovieDao
import com.example.moviecatalogue.data.local.toEntity
import com.example.moviecatalogue.data.remote.ApiService
import com.example.moviecatalogue.domain.AuthRepository
import com.example.moviecatalogue.domain.Genre
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieDetail
import com.example.moviecatalogue.domain.MovieRepository
import com.example.moviecatalogue.domain.MovieVideo
import com.example.moviecatalogue.domain.UserSession
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
    private val authRepository: AuthRepository
) : MovieRepository {

    /** The id of the signed-in account; GUEST_ID when browsing as a guest. */
    private val currentUserId: Int
        get() = authRepository.session.value?.userId ?: UserSession.GUEST_ID

    private val isGuest: Boolean
        get() = currentUserId == UserSession.GUEST_ID

    // ─── Remote Operations ─────────────────────────────────────────────────────

    override suspend fun getTrendingMovies(): Result<List<Movie>> = safeApiCall {
        apiService.getTrendingMovies().results.map { it.toDomain() }
    }

    override suspend fun getNowPlayingMovies(): Result<List<Movie>> = safeApiCall {
        apiService.getNowPlayingMovies().results.map { it.toDomain() }
    }

    override suspend fun getPopularMovies(): Result<List<Movie>> = safeApiCall {
        apiService.getPopularMovies().results.map { it.toDomain() }
    }

    override suspend fun getTopRatedMovies(): Result<List<Movie>> = safeApiCall {
        apiService.getTopRatedMovies().results.map { it.toDomain() }
    }

    override suspend fun searchMovies(query: String): Result<List<Movie>> = safeApiCall {
        apiService.searchMovies(query).results.map { it.toDomain() }
    }

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

    override suspend fun getGenres(): Result<List<Genre>> = safeApiCall {
        apiService.getGenres().genres.map { it.toDomain() }
    }

    // ─── Local (Room) Operations ───────────────────────────────────────────────

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

    // ─── Helper ────────────────────────────────────────────────────────────────

    private suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
        return try {
            Result.success(call())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
