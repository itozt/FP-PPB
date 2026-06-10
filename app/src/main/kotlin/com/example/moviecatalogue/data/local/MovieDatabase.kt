package com.example.moviecatalogue.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room Database configuration for MovFlix.
 * Holds the watchlist_movies table for offline-first capability.
 */
@Database(
    entities = [MovieEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MovieDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

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
