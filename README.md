# MovFlix — Movie Catalogue App

A Netflix-inspired Android app built with **Jetpack Compose**, **MVVM + Repository Pattern**, **Room (offline-first)**, and **TMDb API**.

---

## Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository Pattern |
| Remote | Retrofit 2.11 + OkHttp 4.12 + Gson |
| Local | Room 2.6.1 |
| Images | Coil 2.7.0 |
| Video | AndroidYouTubePlayer 12.1.1 |
| Navigation | Navigation Compose 2.8.3 |
| DI | Manual Service Locator |
| Language | Kotlin 2.0.21 |

---

## Features

- **Home** — Auto-scrolling hero slider (trending) + Now Playing, Popular, Top Rated rows
- **Search** — Debounced live search (500ms) + scrollable genre filter chips
- **Detail** — Backdrop, poster, rating, runtime, genres, synopsis, YouTube trailer dialog
- **Watchlist** — Offline-first Room grid with delete + Undo snackbar
- **Shimmer** — Wave skeleton placeholders on all loading states
- **Dark/Light Mode** — Full Material 3 theming

---

## Setup in Android Studio

### Prerequisites
- **Android Studio Hedgehog (2023.1.1)** or newer (Meerkat recommended)
- **JDK 11** or higher
- Internet connection (for Gradle sync & TMDb API)

---

### Step 1 — Create a New Project in Android Studio

1. Open Android Studio
2. Click **File → New → New Project**
3. Choose **Empty Activity** (the Compose template)
4. Set these values exactly:
   - **Name:** `MovieCatalogue`
   - **Package name:** `com.example.moviecatalogue`
   - **Save location:** your chosen directory
   - **Language:** Kotlin
   - **Minimum SDK:** API 24
5. Click **Finish** and wait for the initial Gradle sync

---

### Step 2 — Replace Project Files

Replace **every** generated file with the files from this project. The complete structure is:

```
MovieCatalogue/
├── gradle/
│   ├── libs.versions.toml          ← Version catalog
│   └── wrapper/
│       └── gradle-wrapper.properties
├── app/
│   ├── build.gradle.kts            ← App-level Gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/example/moviecatalogue/
│       │   ├── MainActivity.kt
│       │   ├── MovieCatalogueApp.kt
│       │   ├── data/
│       │   │   ├── local/
│       │   │   │   ├── MovieDao.kt
│       │   │   │   ├── MovieDatabase.kt
│       │   │   │   └── MovieEntity.kt
│       │   │   ├── remote/
│       │   │   │   ├── ApiService.kt
│       │   │   │   └── Dtos.kt
│       │   │   └── repository/
│       │   │       └── MovieRepositoryImpl.kt
│       │   ├── di/
│       │   │   └── ServiceLocator.kt
│       │   ├── domain/
│       │   │   ├── Movie.kt
│       │   │   └── MovieRepository.kt
│       │   └── ui/
│       │       ├── components/
│       │       │   ├── MovieCard.kt
│       │       │   └── MovieSlider.kt
│       │       ├── navigation/
│       │       │   └── Navigation.kt
│       │       ├── screens/
│       │       │   ├── detail/
│       │       │   │   ├── DetailScreen.kt
│       │       │   │   └── DetailViewModel.kt
│       │       │   ├── home/
│       │       │   │   ├── HomeScreen.kt
│       │       │   │   └── HomeViewModel.kt
│       │       │   ├── search/
│       │       │   │   ├── SearchScreen.kt
│       │       │   │   └── SearchViewModel.kt
│       │       │   └── watchlist/
│       │       │       ├── WatchlistScreen.kt
│       │       │       └── WatchlistViewModel.kt
│       │       └── theme/
│       │           ├── Color.kt
│       │           ├── Theme.kt
│       │           └── Typography.kt
│       └── res/
│           ├── values/
│           │   ├── strings.xml
│           │   └── themes.xml
│           └── xml/
│               └── network_security_config.xml
├── build.gradle.kts                ← Root Gradle
├── settings.gradle.kts
└── gradle.properties
```

**How to replace files:**
- In Android Studio's **Project** panel (left sidebar), switch the view to **Project** (not Android)
- Navigate to each file, right-click → **Delete**, then right-click the folder → **New → Kotlin Class/File** to create replacement files
- Paste the file contents from this project

**Tip:** For the `gradle/` and `res/` files, you can use the OS file explorer to paste directly into the project folder, then click **Sync Now** in Android Studio.

---

### Step 3 — Sync Gradle

After copying all files:

1. Click the **"Sync Now"** yellow bar that appears at the top of the editor, **OR**
2. Go to **File → Sync Project with Gradle Files**

Android Studio will download all dependencies (~200 MB first time). Wait for the sync to complete (check the bottom status bar).

---

### Step 4 — Add a Launcher Icon (Optional but Recommended)

The app references `@mipmap/ic_launcher`. Android Studio generates a default one, so it will compile fine. To add a custom icon:

1. Right-click `app/src/main/res` → **New → Image Asset**
2. Choose your icon image
3. Click **Next → Finish**

---

### Step 5 — Run the App

1. Connect an Android device (API 24+) via USB with **USB Debugging** enabled, **OR** create an emulator via **Device Manager** (AVD)
2. Select your device in the toolbar dropdown
3. Click the **▶ Run** button (Shift+F10)

The app will build and install. First launch fetches data from TMDb.

---

## Troubleshooting

### "Unresolved reference" errors after pasting files
→ Check the **package name** at the top of each file matches `com.example.moviecatalogue`. If you used a different package name when creating the project, do a global **Find & Replace** (Ctrl+Shift+R) to change `com.example.moviecatalogue` to your package.

### Gradle sync fails — "Could not resolve..."
→ Check your internet connection. Then: **File → Invalidate Caches → Invalidate and Restart**.

### App crashes on launch — "Network error"
→ The device/emulator has no internet. Check the emulator's network settings or test on a real device.

### YouTube player shows black screen
→ YouTube requires Google Play Services. Use a physical device or an emulator with Play Store enabled (choose a "Google Play" system image in the AVD wizard).

### Build error: "KSP not configured"
→ Ensure the root `build.gradle.kts` includes `alias(libs.plugins.kotlin.ksp) apply false` and the app `build.gradle.kts` has `alias(libs.plugins.kotlin.ksp)`.

---

## API Credentials

The TMDb API key and Bearer Token are already embedded in `BuildConfig` via `app/build.gradle.kts`. No additional configuration is needed.

```
Base URL:  https://api.themoviedb.org/3/
Image URL: https://image.tmdb.org/t/p/w500/
```

---

## Architecture Overview

```
UI Layer (Compose Screens)
    ↓ collectAsStateWithLifecycle
ViewModel (StateFlow<UiState>)
    ↓ suspend functions / Flow
Repository Interface (domain)
    ↑ implements
MovieRepositoryImpl (data)
    ├── ApiService (Retrofit) — remote data
    └── MovieDao (Room) — local data / offline cache
```

Data flows **one-way**: Repository → ViewModel → UI. The Room database is the **Single Source of Truth** for the Watchlist, making it fully offline-capable.
