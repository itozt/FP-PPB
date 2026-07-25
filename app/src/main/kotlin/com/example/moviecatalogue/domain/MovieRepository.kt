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

    suspend fun getPopularMovies(page: Int = 1): Result<List<Movie>>

    suspend fun getTopRatedMovies(): Result<List<Movie>>

    // ─── Remote: TV Series Categories ─────────────────────────────────────────

    suspend fun getPopularTvShows(): Result<List<Movie>>

    suspend fun getTopRatedTvShows(): Result<List<Movie>>

    // ─── Remote: Search & Discover ────────────────────────────────────────────

    suspend fun searchMulti(query: String, page: Int = 1): Result<List<Movie>>

    suspend fun discoverMoviesByGenre(genreId: Int, page: Int = 1): Result<List<Movie>>

    // ─── Remote: Detail ───────────────────────────────────────────────────────

    suspend fun getMovieDetail(movieId: Int): Result<MovieDetail>

    suspend fun getMovieVideos(movieId: Int): Result<List<MovieVideo>>

    // ─── Remote: TV Series Detail ─────────────────────────────────────────────

    suspend fun getTvDetail(tvId: Int): Result<MovieDetail>

    suspend fun getTvSeasonEpisodes(tvId: Int, seasonNumber: Int): Result<List<TvEpisode>>

    // ─── Local: Watchlist (Room) ───────────────────────────────────────────────

    fun getWatchlist(): Flow<List<Movie>>

    suspend fun addToWatchlist(movie: Movie)

    suspend fun removeFromWatchlist(movieId: Int)

    suspend fun isInWatchlist(movieId: Int): Boolean

    fun isInWatchlistFlow(movieId: Int): Flow<Boolean>

    // ─── Remote: Genres ───────────────────────────────────────────────────────

    suspend fun getGenres(): Result<List<Genre>>

    // ─── Local: Watch Progress ────────────────────────────────────────────────

    suspend fun saveWatchProgress(progress: WatchProgress)

    suspend fun getWatchProgress(contentId: Int, mediaType: MediaType, season: Int? = null, episode: Int? = null): WatchProgress?

    fun getWatchProgressFlow(contentId: Int, mediaType: MediaType): Flow<WatchProgress?>

    fun getContinueWatching(): Flow<List<WatchProgress>>
}
