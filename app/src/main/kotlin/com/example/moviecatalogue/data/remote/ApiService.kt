package com.example.moviecatalogue.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API service interface for TMDb endpoints.
 */
interface ApiService {

    // ─── Movie Endpoints ──────────────────────────────────────────────────────

    @GET("trending/all/week")
    suspend fun getTrendingAll(): TrendingAllResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int
    ): MovieDetailDTO

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int
    ): VideoResponse

    // ─── TV Series Endpoints ──────────────────────────────────────────────────

    @GET("tv/popular")
    suspend fun getPopularTvShows(
        @Query("page") page: Int = 1
    ): TvResponse

    @GET("tv/top_rated")
    suspend fun getTopRatedTvShows(
        @Query("page") page: Int = 1
    ): TvResponse

    @GET("tv/{series_id}")
    suspend fun getTvDetail(
        @Path("series_id") seriesId: Int
    ): TvDetailDTO

    @GET("tv/{series_id}/season/{season_number}")
    suspend fun getTvSeasonDetail(
        @Path("series_id") seriesId: Int,
        @Path("season_number") seasonNumber: Int
    ): TvSeasonDetailDTO

    @GET("tv/{series_id}/videos")
    suspend fun getTvVideos(
        @Path("series_id") seriesId: Int
    ): VideoResponse

    // ─── Search ───────────────────────────────────────────────────────────────

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): MultiSearchResponse

    // ─── Discover ─────────────────────────────────────────────────────────────

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int = 1
    ): MovieResponse

    // ─── Genres ───────────────────────────────────────────────────────────────

    @GET("genre/movie/list")
    suspend fun getGenres(): GenreResponse
}
