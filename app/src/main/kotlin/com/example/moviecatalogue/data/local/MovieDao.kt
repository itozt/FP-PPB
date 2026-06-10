package com.example.moviecatalogue.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for watchlist movie operations in Room Database.
 */
@Dao
interface MovieDao {

    @Query("SELECT * FROM watchlist_movies ORDER BY addedAt DESC")
    fun getAllWatchlistMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM watchlist_movies WHERE id = :movieId LIMIT 1")
    suspend fun getMovieById(movieId: Int): MovieEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_movies WHERE id = :movieId)")
    suspend fun isInWatchlist(movieId: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_movies WHERE id = :movieId)")
    fun isInWatchlistFlow(movieId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Delete
    suspend fun deleteMovie(movie: MovieEntity)

    @Query("DELETE FROM watchlist_movies WHERE id = :movieId")
    suspend fun deleteMovieById(movieId: Int)

    @Query("SELECT COUNT(*) FROM watchlist_movies")
    fun getWatchlistCount(): Flow<Int>
}
