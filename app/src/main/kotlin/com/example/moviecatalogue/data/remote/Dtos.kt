package com.example.moviecatalogue.data.remote

import com.google.gson.annotations.SerializedName
import com.example.moviecatalogue.domain.Genre
import com.example.moviecatalogue.domain.MediaType
import com.example.moviecatalogue.domain.Movie
import com.example.moviecatalogue.domain.MovieDetail
import com.example.moviecatalogue.domain.MovieVideo
import com.example.moviecatalogue.domain.TvEpisode
import com.example.moviecatalogue.domain.TvSeason

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
        adult = adult,
        mediaType = MediaType.MOVIE
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
        adult = adult,
        mediaType = MediaType.MOVIE
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

// ─── TV Series Detail ─────────────────────────────────────────────────────────

data class TvDetailDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String = "",
    @SerializedName("overview") val overview: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("first_air_date") val firstAirDate: String = "",
    @SerializedName("vote_average") val voteAverage: Double = 0.0,
    @SerializedName("vote_count") val voteCount: Int = 0,
    @SerializedName("genres") val genres: List<GenreDTO> = emptyList(),
    @SerializedName("popularity") val popularity: Double = 0.0,
    @SerializedName("original_language") val originalLanguage: String = "",
    @SerializedName("number_of_seasons") val numberOfSeasons: Int = 0,
    @SerializedName("number_of_episodes") val numberOfEpisodes: Int = 0,
    @SerializedName("status") val status: String = "",
    @SerializedName("tagline") val tagline: String = "",
    @SerializedName("seasons") val seasons: List<TvSeasonDTO> = emptyList(),
    @SerializedName("episode_run_time") val episodeRunTime: List<Int> = emptyList()
) {
    fun toDomain(): Movie = Movie(
        id = id,
        title = name,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = firstAirDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genres = genres.map { it.toDomain() },
        genreIds = genres.map { it.id },
        popularity = popularity,
        originalLanguage = originalLanguage,
        mediaType = MediaType.TV
    )
}

data class TvSeasonDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("season_number") val seasonNumber: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("overview") val overview: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("episode_count") val episodeCount: Int = 0,
    @SerializedName("air_date") val airDate: String = ""
) {
    fun toDomain(): TvSeason = TvSeason(
        id = id,
        seasonNumber = seasonNumber,
        name = name,
        overview = overview,
        posterPath = posterPath,
        episodeCount = episodeCount,
        airDate = airDate
    )
}

data class TvSeasonDetailDTO(
    @SerializedName("episodes") val episodes: List<TvEpisodeDTO> = emptyList()
)

data class TvEpisodeDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("episode_number") val episodeNumber: Int = 0,
    @SerializedName("season_number") val seasonNumber: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("overview") val overview: String = "",
    @SerializedName("still_path") val stillPath: String? = null,
    @SerializedName("air_date") val airDate: String? = null,
    @SerializedName("runtime") val runtime: Int? = null,
    @SerializedName("vote_average") val voteAverage: Double = 0.0
) {
    fun toDomain(): TvEpisode = TvEpisode(
        id = id,
        episodeNumber = episodeNumber,
        seasonNumber = seasonNumber,
        name = name,
        overview = overview,
        stillPath = stillPath,
        airDate = airDate,
        runtime = runtime,
        voteAverage = voteAverage
    )
}

// ─── TV List Response ─────────────────────────────────────────────────────────

data class TvResponse(
    @SerializedName("results") val results: List<TvListDTO> = emptyList(),
    @SerializedName("page") val page: Int = 1,
    @SerializedName("total_pages") val totalPages: Int = 1
)

data class TvListDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String = "",
    @SerializedName("overview") val overview: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("first_air_date") val firstAirDate: String = "",
    @SerializedName("vote_average") val voteAverage: Double = 0.0,
    @SerializedName("vote_count") val voteCount: Int = 0,
    @SerializedName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerializedName("popularity") val popularity: Double = 0.0,
    @SerializedName("original_language") val originalLanguage: String = ""
) {
    fun toDomain(): Movie = Movie(
        id = id,
        title = name,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = firstAirDate,
        voteAverage = voteAverage,
        voteCount = voteCount,
        genreIds = genreIds,
        popularity = popularity,
        originalLanguage = originalLanguage,
        mediaType = MediaType.TV
    )
}

// ─── Multi-Search Response (movie + tv mixed) ────────────────────────────────

data class MultiSearchResponse(
    @SerializedName("results") val results: List<MultiSearchDTO> = emptyList(),
    @SerializedName("page") val page: Int = 1,
    @SerializedName("total_pages") val totalPages: Int = 1,
    @SerializedName("total_results") val totalResults: Int = 0
)

data class MultiSearchDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("media_type") val mediaType: String = "",
    // Movie-specific
    @SerializedName("title") val title: String? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    // TV-specific
    @SerializedName("name") val name: String? = null,
    @SerializedName("first_air_date") val firstAirDate: String? = null,
    // Shared
    @SerializedName("overview") val overview: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("vote_average") val voteAverage: Double = 0.0,
    @SerializedName("vote_count") val voteCount: Int = 0,
    @SerializedName("genre_ids") val genreIds: List<Int> = emptyList(),
    @SerializedName("popularity") val popularity: Double = 0.0,
    @SerializedName("original_language") val originalLanguage: String = "",
    @SerializedName("adult") val adult: Boolean = false
) {
    fun toDomain(): Movie? {
        val type = MediaType.fromString(mediaType)
        // Filter out "person" and unsupported types
        if (mediaType != "movie" && mediaType != "tv") return null

        return Movie(
            id = id,
            title = if (type == MediaType.TV) (name ?: "") else (title ?: ""),
            overview = overview,
            posterPath = posterPath,
            backdropPath = backdropPath,
            releaseDate = if (type == MediaType.TV) (firstAirDate ?: "") else (releaseDate ?: ""),
            voteAverage = voteAverage,
            voteCount = voteCount,
            genreIds = genreIds,
            popularity = popularity,
            originalLanguage = originalLanguage,
            adult = adult,
            mediaType = type
        )
    }
}

// ─── Trending All Response ────────────────────────────────────────────────────

data class TrendingAllResponse(
    @SerializedName("results") val results: List<MultiSearchDTO> = emptyList(),
    @SerializedName("page") val page: Int = 1,
    @SerializedName("total_pages") val totalPages: Int = 1
)
