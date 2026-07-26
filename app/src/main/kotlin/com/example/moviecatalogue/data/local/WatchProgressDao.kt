package com.example.moviecatalogue.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for watch progress operations in Room Database.
 */
@Dao
interface WatchProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: WatchProgressEntity)

    @Query("""
        SELECT * FROM watch_progress 
        WHERE contentId = :contentId AND userId = :userId AND mediaType = :mediaType 
              AND season = :season AND episode = :episode
        LIMIT 1
    """)
    suspend fun getProgress(
        contentId: Int,
        userId: Int,
        mediaType: String,
        season: Int = 0,
        episode: Int = 0
    ): WatchProgressEntity?

    @Query("""
        SELECT * FROM watch_progress 
        WHERE contentId = :contentId AND userId = :userId AND mediaType = :mediaType
        ORDER BY lastWatched DESC
        LIMIT 1
    """)
    fun getLatestProgressFlow(
        contentId: Int,
        userId: Int,
        mediaType: String
    ): Flow<WatchProgressEntity?>

    @Query("""
        SELECT * FROM watch_progress 
        WHERE userId = :userId AND progress > 0.0 AND progress < 95.0
        GROUP BY contentId, mediaType
        ORDER BY lastWatched DESC
        LIMIT 10
    """)
    fun getContinueWatching(userId: Int): Flow<List<WatchProgressEntity>>
}
