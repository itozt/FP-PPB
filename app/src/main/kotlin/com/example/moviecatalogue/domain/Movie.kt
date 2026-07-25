package com.example.moviecatalogue.domain

/**
 * Media type discriminator used throughout the app.
 * Determines whether a content item is a Movie or a TV Series.
 */
enum class MediaType(val value: String) {
    MOVIE("movie"),
    TV("tv");

    companion object {
        fun fromString(value: String): MediaType =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: MOVIE
    }
}

/**
 * Domain model representing a Movie or TV Series for the UI layer.
 * This is the single source of truth model used throughout the app.
 */
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val genreIds: List<Int> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val popularity: Double = 0.0,
    val originalLanguage: String = "",
    val adult: Boolean = false,
    val mediaType: MediaType = MediaType.MOVIE
) {
    /** Unified display title — works for both movies (title) and TV (title is mapped from name). */
    val displayTitle: String
        get() = title.ifBlank { "Untitled" }

    val posterUrl: String
        get() = if (!posterPath.isNullOrBlank()) "https://image.tmdb.org/t/p/w500$posterPath" else ""

    val backdropUrl: String
        get() = if (!backdropPath.isNullOrBlank()) "https://image.tmdb.org/t/p/w500$backdropPath" else ""

    val formattedRating: String
        get() = String.format("%.1f", voteAverage)

    val releaseYear: String
        get() = releaseDate.take(4)

    val isTvSeries: Boolean
        get() = mediaType == MediaType.TV
}

data class Genre(
    val id: Int,
    val name: String
)

data class MovieVideo(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val official: Boolean
)

// ─── TV Series Models ─────────────────────────────────────────────────────────

data class TvSeason(
    val id: Int,
    val seasonNumber: Int,
    val name: String,
    val overview: String = "",
    val posterPath: String? = null,
    val episodeCount: Int = 0,
    val airDate: String = ""
)

data class TvEpisode(
    val id: Int,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val name: String,
    val overview: String = "",
    val stillPath: String? = null,
    val airDate: String? = null,
    val runtime: Int? = null,
    val voteAverage: Double = 0.0
) {
    val stillUrl: String
        get() = if (!stillPath.isNullOrBlank()) "https://image.tmdb.org/t/p/w300$stillPath" else ""

    val formattedRuntime: String
        get() = runtime?.let { "${it}m" } ?: "N/A"
}

// ─── Movie Detail ─────────────────────────────────────────────────────────────

data class MovieDetail(
    val movie: Movie,
    val videos: List<MovieVideo> = emptyList(),
    val runtime: Int = 0,
    val tagline: String = "",
    val status: String = "",
    // TV Series metadata
    val numberOfSeasons: Int = 0,
    val seasons: List<TvSeason> = emptyList()
) {
    val isTvSeries: Boolean get() = movie.mediaType == MediaType.TV

    /**
     * The single YouTube trailer to play — chosen to match what themoviedb.org
     * shows. TMDB's API returns videos newest-first, so we take the first
     * official "Trailer", falling back to any "Trailer" only if none are
     * marked official. We intentionally do NOT fall through to teasers or
     * featurettes, so the app plays the *same* clip as TMDB instead of
     * silently substituting a different video.
     */
    val trailerKey: String?
        get() {
            val trailers = videos.filter {
                it.site.equals("YouTube", ignoreCase = true) &&
                    it.type.equals("Trailer", ignoreCase = true) &&
                    it.key.isValidYouTubeVideoId()
            }
            return (trailers.firstOrNull { it.official } ?: trailers.firstOrNull())?.key
        }

    /** Single-element list (or empty) kept for the player's existing API. */
    val trailerCandidates: List<String>
        get() = listOfNotNull(trailerKey)

    val formattedRuntime: String
        get() = if (runtime > 0) "${runtime / 60}h ${runtime % 60}m" else "N/A"

    /** Playable seasons — filters out season 0 (Specials) if it has 0 episodes. */
    val playableSeasons: List<TvSeason>
        get() = seasons.filter { it.episodeCount > 0 }

    private fun String.isValidYouTubeVideoId(): Boolean {
        return isNotBlank() && matches(Regex("^[A-Za-z0-9_-]{11}$"))
    }
}

// ─── Watch Progress ───────────────────────────────────────────────────────────

/**
 * Tracks how far a user has watched a movie or a specific TV episode.
 */
data class WatchProgress(
    val contentId: Int,
    val mediaType: MediaType,
    val currentTime: Double,
    val duration: Double,
    val progress: Double,
    val title: String,
    val posterUrl: String,
    val season: Int? = null,
    val episode: Int? = null,
    val lastWatched: Long = System.currentTimeMillis()
)
