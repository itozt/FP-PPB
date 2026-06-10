package com.example.moviecatalogue.domain

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining all data operations for the domain layer.
 * Abstracts the data sources (remote API and local Room database).
 */
interface MovieRepository {

    // ─── Remote: Trending & Categories ────────────────────────────────────────

    suspend fun getTrendingMovies(): Result<List<Movie>>

    suspend fun getNowPlayingMovies(): Result<List<Movie>>

    suspend fun getPopularMovies(): Result<List<Movie>>

    suspend fun getTopRatedMovies(): Result<List<Movie>>

    // ─── Remote: Search ───────────────────────────────────────────────────────

    suspend fun searchMovies(query: String): Result<List<Movie>>

    // ─── Remote: Detail ───────────────────────────────────────────────────────

    suspend fun getMovieDetail(movieId: Int): Result<MovieDetail>

    suspend fun getMovieVideos(movieId: Int): Result<List<MovieVideo>>

    // ─── Local: Watchlist (Room) ───────────────────────────────────────────────

    fun getWatchlist(): Flow<List<Movie>>

    suspend fun addToWatchlist(movie: Movie)

    suspend fun removeFromWatchlist(movieId: Int)

    suspend fun isInWatchlist(movieId: Int): Boolean

    fun isInWatchlistFlow(movieId: Int): Flow<Boolean>

    // ─── Remote: Genres ───────────────────────────────────────────────────────

    suspend fun getGenres(): Result<List<Genre>>
}
