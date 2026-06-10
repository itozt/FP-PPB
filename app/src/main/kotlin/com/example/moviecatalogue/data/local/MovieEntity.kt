package com.example.moviecatalogue.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.moviecatalogue.domain.Genre
import com.example.moviecatalogue.domain.Movie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room entity for persisting watchlist movies locally.
 * Enables full offline access to saved movies.
 */
@Entity(tableName = "watchlist_movies")
@TypeConverters(Converters::class)
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val genreIds: List<Int>,
    val genres: List<Genre>,
    val popularity: Double,
    val originalLanguage: String,
    val adult: Boolean,
    val addedAt: Long = System.currentTimeMillis()
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
        genres = genres,
        popularity = popularity,
        originalLanguage = originalLanguage,
        adult = adult
    )
}

fun Movie.toEntity(): MovieEntity = MovieEntity(
    id = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
    genreIds = genreIds,
    genres = genres,
    popularity = popularity,
    originalLanguage = originalLanguage,
    adult = adult
)

/**
 * Type converters for Room to handle complex types (Lists).
 */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromIntList(value: List<Int>): String = gson.toJson(value)

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromGenreList(value: List<Genre>): String = gson.toJson(value)

    @TypeConverter
    fun toGenreList(value: String): List<Genre> {
        val type = object : TypeToken<List<Genre>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
