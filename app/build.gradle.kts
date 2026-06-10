plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.example.moviecatalogue"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.moviecatalogue"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "TMDB_API_KEY", "\"ef984ba89727a62c438a7113c99964c0\"")
        buildConfigField(
            "String",
            "TMDB_BEARER_TOKEN",
            "\"eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJlZjk4NGJhODk3MjdhNjJjNDM4YTcxMTNjOTk5NjRjMCIsIm5iZiI6MTc4MDcyNzE5Ny42NDIsInN1YiI6IjZhMjNiZDlkMTFkNzM4NTdhZmYxYzYxMCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.vJGb_C5C0IV1oHsl4HLxV3gvn1TwSAz_I-ZqdvILL8U\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Lifecycle / ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coil
    implementation(libs.coil.compose)

    // YouTube Player
    implementation(libs.youtube.player)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
