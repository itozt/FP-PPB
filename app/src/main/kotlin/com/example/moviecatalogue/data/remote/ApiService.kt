package com.example.moviecatalogue.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API service interface for TMDb endpoints.
 */
interface ApiService {

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(): MovieResponse

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

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
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

    @GET("genre/movie/list")
    suspend fun getGenres(): GenreResponse
}
