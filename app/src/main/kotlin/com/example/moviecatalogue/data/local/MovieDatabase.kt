package com.example.moviecatalogue.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room Database configuration for MovFlix.
 * Holds the watchlist_movies and watch_progress tables for offline-first capability.
 */
@Database(
    entities = [MovieEntity::class, UserEntity::class, WatchProgressEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MovieDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

    abstract fun userDao(): UserDao

    abstract fun watchProgressDao(): WatchProgressDao

    companion object {
        private const val DATABASE_NAME = "movflix_db"

        @Volatile
        private var INSTANCE: MovieDatabase? = null

        fun getInstance(context: Context): MovieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
