package com.example.moviecatalogue.di

import android.content.Context
import com.example.moviecatalogue.BuildConfig
import com.example.moviecatalogue.data.local.MovieDatabase
import com.example.moviecatalogue.data.remote.ApiService
import com.example.moviecatalogue.data.repository.MovieRepositoryImpl
import com.example.moviecatalogue.domain.MovieRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Manual Service Locator providing clean dependency injection
 * without the overhead of Hilt/Dagger for this project scope.
 *
 * Initialized in MovieCatalogueApp, accessible app-wide.
 */
object ServiceLocator {

    private const val BASE_URL = "https://api.themoviedb.org/3/"

    @Volatile
    private var repository: MovieRepository? = null

    fun provideRepository(context: Context): MovieRepository {
        return repository ?: synchronized(this) {
            repository ?: buildRepository(context).also { repository = it }
        }
    }

    private fun buildRepository(context: Context): MovieRepository {
        val apiService = buildApiService()
        val database = MovieDatabase.getInstance(context)
        return MovieRepositoryImpl(
            apiService = apiService,
            movieDao = database.movieDao()
        )
    }

    private fun buildApiService(): ApiService {
        val client = buildOkHttpClient()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private fun buildOkHttpClient(): OkHttpClient {
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Authorization", "Bearer ${BuildConfig.TMDB_BEARER_TOKEN}")
                .header("accept", "application/json")
                .build()
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
