package com.example.moviecatalogue.data.repository

import com.example.moviecatalogue.data.local.MovieDao
import com.example.moviecatalogue.data.local.toEntity
import com.example.moviecatalogue.data.remote.ApiService
import com.example.moviecatalogue.domain.Genre
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieDetail
import com.example.moviecatalogue.domain.MovieRepository
import com.example.moviecatalogue.domain.MovieVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Concrete implementation of MovieRepository.
 * Bridges the remote API and local Room database,
 * acting as the Single Source of Truth.
 */
class MovieRepositoryImpl(
    private val apiService: ApiService,
    private val movieDao: MovieDao
) : MovieRepository {

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

    override fun getWatchlist(): Flow<List<Movie>> =
        movieDao.getAllWatchlistMovies().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addToWatchlist(movie: Movie) {
        movieDao.insertMovie(movie.toEntity())
    }

    override suspend fun removeFromWatchlist(movieId: Int) {
        movieDao.deleteMovieById(movieId)
    }

    override suspend fun isInWatchlist(movieId: Int): Boolean =
        movieDao.isInWatchlist(movieId)

    override fun isInWatchlistFlow(movieId: Int): Flow<Boolean> =
        movieDao.isInWatchlistFlow(movieId)

    // ─── Helper ────────────────────────────────────────────────────────────────

    private suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
        return try {
            Result.success(call())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
