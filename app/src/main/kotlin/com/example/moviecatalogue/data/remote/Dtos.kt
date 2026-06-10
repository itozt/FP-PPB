package com.example.moviecatalogue.data.remote

import com.google.gson.annotations.SerializedName
import com.example.moviecatalogue.domain.Genre
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieDetail
import com.example.moviecatalogue.domain.MovieVideo

// ─── Movie List Response ──────────────────────────────────────────────────────

data class MovieResponse(
    @SerializedName("results") val results: List<MovieDTO> = emptyList(),
    @SerializedName("page") val page: Int = 1,
    @SerializedName("total_pages") val totalPages: Int = 1,
    @SerializedName("total_results") val totalResults: Int = 0
)

data class MovieDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String = "",
    @SerializedName("overview") val overview: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("release_date") val releaseDate: String = "",
    @SerializedName("vote_average") val voteAverage: Double = 0.0,
    @SerializedName("vote_count") val voteCount: Int = 0,
    @SerializedName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerializedName("popularity") val popularity: Double = 0.0,
    @SerializedName("original_language") val originalLanguage: String = "",
    @SerializedName("adult") val adult: Boolean = false
) {
    fun toDomain(): Movie = Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genreIds = genreIds,
        popularity = popularity,
        originalLanguage = originalLanguage,
        adult = adult
    )
}

// ─── Movie Detail Response ────────────────────────────────────────────────────

data class MovieDetailDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String = "",
    @SerializedName("overview") val overview: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("release_date") val releaseDate: String = "",
    @SerializedName("vote_average") val voteAverage: Double = 0.0,
    @SerializedName("vote_count") val voteCount: Int = 0,
    @SerializedName("genres") val genres: List<GenreDTO> = emptyList(),
    @SerializedName("popularity") val popularity: Double = 0.0,
    @SerializedName("original_language") val originalLanguage: String = "",
    @SerializedName("runtime") val runtime: Int = 0,
    @SerializedName("tagline") val tagline: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("adult") val adult: Boolean = false
) {
    fun toDomain(): Movie = Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres.map { it.toDomain() },
        genreIds = genres.map { it.id },
        popularity = popularity,
        originalLanguage = originalLanguage,
        adult = adult
    )
}

// ─── Genre ────────────────────────────────────────────────────────────────────

data class GenreDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String = ""
) {
    fun toDomain(): Genre = Genre(id = id, name = name)
}

data class GenreResponse(
    @SerializedName("genres") val genres: List<GenreDTO> = emptyList()
)

// ─── Video Response ───────────────────────────────────────────────────────────

data class VideoResponse(
    @SerializedName("results") val results: List<VideoDTO> = emptyList(),
    @SerializedName("id") val id: Int = 0
)

data class VideoDTO(
    @SerializedName("id") val id: String = "",
    @SerializedName("key") val key: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("site") val site: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("official") val official: Boolean = false
) {
    fun toDomain(): MovieVideo = MovieVideo(
        id = id,
        key = key,
        name = name,
        site = site,
        type = type,
        official = official
    )
}
