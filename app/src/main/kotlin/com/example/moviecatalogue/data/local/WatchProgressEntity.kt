package com.example.moviecatalogue.data.local

import androidx.room.Entity
import com.example.moviecatalogue.domain.MediaType
import com.example.moviecatalogue.domain.WatchProgress

/**
 * Room entity for persisting watch progress locally, scoped per user account.
 *
 * For movies: season and episode are null.
 * For TV: season and episode identify the specific episode being tracked.
 *
 * Composite PK: [contentId, userId, mediaType, season, episode] ensures
 * each episode of each show per user has its own progress record.
 */
@Entity(
    tableName = "watch_progress",
    primaryKeys = ["contentId", "userId", "mediaType", "season", "episode"]
)
data class WatchProgressEntity(
    val contentId: Int,
    val userId: Int,
    val mediaType: String,
    val currentTime: Double,
    val duration: Double,
    val progress: Double,
    val title: String,
    val posterUrl: String,
    val season: Int = 0,
    val episode: Int = 0,
    val lastWatched: Long = System.currentTimeMillis()
) {
    fun toDomain(): WatchProgress = WatchProgress(
        contentId = contentId,
        mediaType = MediaType.fromString(mediaType),
        currentTime = currentTime,
        duration = duration,
        progress = progress,
        title = title,
        posterUrl = posterUrl,
        season = if (season > 0) season else null,
        episode = if (episode > 0) episode else null,
        lastWatched = lastWatched
    )
}

fun WatchProgress.toEntity(userId: Int): WatchProgressEntity = WatchProgressEntity(
    contentId = contentId,
    userId = userId,
    mediaType = mediaType.value,
    currentTime = currentTime,
    duration = duration,
    progress = progress,
    title = title,
    posterUrl = posterUrl,
    season = season ?: 0,
    episode = episode ?: 0,
    lastWatched = lastWatched
)
