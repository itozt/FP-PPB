package com.example.moviecatalogue.domain

/**
 * Domain model representing a Movie for the UI layer.
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
    val adult: Boolean = false
) {
    val posterUrl: String
        get() = if (!posterPath.isNullOrBlank()) "https://image.tmdb.org/t/p/w500$posterPath" else ""

    val backdropUrl: String
        get() = if (!backdropPath.isNullOrBlank()) "https://image.tmdb.org/t/p/w500$backdropPath" else ""

    val formattedRating: String
        get() = String.format("%.1f", voteAverage)

    val releaseYear: String
        get() = releaseDate.take(4)
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

data class MovieDetail(
    val movie: Movie,
    val videos: List<MovieVideo> = emptyList(),
    val runtime: Int = 0,
    val tagline: String = "",
    val status: String = ""
) {
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

    private fun String.isValidYouTubeVideoId(): Boolean {
        return isNotBlank() && matches(Regex("^[A-Za-z0-9_-]{11}$"))
    }
}
