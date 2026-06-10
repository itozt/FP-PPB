package com.example.moviecatalogue

import android.app.Application

/**
 * Application class — entry point for app-level initialization.
 */
class MovieCatalogueApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // ServiceLocator is lazily initialized on first use
        // but we hold a reference to the context here for convenience.
    }
}
