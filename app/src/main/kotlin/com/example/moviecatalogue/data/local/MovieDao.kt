package com.example.moviecatalogue.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for watchlist movie operations in Room Database.
 * All watchlist queries are scoped to a specific userId (the signed-in account).
 */
@Dao
interface MovieDao {

    @Query("SELECT * FROM watchlist_movies WHERE userId = :userId ORDER BY addedAt DESC")
    fun getAllWatchlistMovies(userId: Int): Flow<List<MovieEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_movies WHERE id = :movieId AND userId = :userId)")
    suspend fun isInWatchlist(movieId: Int, userId: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_movies WHERE id = :movieId AND userId = :userId)")
    fun isInWatchlistFlow(movieId: Int, userId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Query("DELETE FROM watchlist_movies WHERE id = :movieId AND userId = :userId")
    suspend fun deleteMovieById(movieId: Int, userId: Int)

    @Query("SELECT COUNT(*) FROM watchlist_movies WHERE userId = :userId")
    fun getWatchlistCount(userId: Int): Flow<Int>
}
