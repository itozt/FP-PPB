<p align="center">
  <img name="download-badge" src="app/src/main/res/drawable/movflix_logo.png" alt="MovFlix Logo" width="250" style="background-color: black; padding: 40px 25%; border-radius: 12px;">
</p>

<h1 align="center">MovFlix</h1>

<p align="center">
  <a href="https://github.com/itozt/FP-PPB/releases/latest"><img src="https://img.shields.io/github/v/tag/itozt/FP-PPB?color=brightgreen&label=version&style=for-the-badge" alt="Latest Version"></a>
  &nbsp;
  <a href="https://github.com/itozt/FP-PPB/releases/latest/download/movflix_v1.0.0.apk"><img src="https://img.shields.io/badge/download_apk-blue?style=for-the-badge&logo=android" alt="Download APK"></a>
</p>

> Aplikasi katalog film modern untuk menjelajah, mencari, dan menonton trailer film terbaru, dibangun sepenuhnya menggunakan teknologi Jetpack Compose dengan data real-time dari TMDB.

## 📖 Tentang
**MovFlix** adalah aplikasi *movie catalogue* yang menampilkan daftar film populer, detail film, serta pemutaran *trailer* langsung di dalam aplikasi. Seluruh data film diambil secara *real-time* dari **TMDB (The Movie Database) API**. Aplikasi ini menghadirkan navigasi yang responsif berbasis gestur *swipe*, dilengkapi sistem **autentikasi lokal** (login, register, dan guest) serta **watchlist** yang tersimpan terpisah untuk setiap akun secara *offline* di perangkat. Dikembangkan sebagai pemenuhan evaluasi Final Project mata kuliah Pemrograman Perangkat Bergerak.

## ✨ Fitur Utama
- **Autentikasi Lokal:** Register dan login dengan email & password, atau **Masuk sebagai Guest** tanpa akun. Password diamankan dengan *hashing* **SHA-256 + salt** dan disimpan secara lokal melalui Room.
- **Navigasi Cepat Berbasis Swipe:** Transisi mulus antara layar **Home**, **Search**, dan **Profile** menggunakan HorizontalPager yang tersinkron dengan *bottom navigation*.
- **Beranda Dinamis:** *Hero slider* film *trending* yang bergeser otomatis dan tak terbatas (*infinite loop*), diikuti baris kategori **Now Playing**, **Popular**, dan **Top Rated**.
- **Pencarian Cerdas:** Pencarian film secara *debounced* (tanpa tombol submit) lengkap dengan filter **genre**.
- **Detail & Trailer:** Halaman detail film menampilkan sinopsis, rating, durasi, dan genre, serta pemutaran **trailer YouTube** *full-screen* langsung di dalam aplikasi.
- **Watchlist Per-Akun:** Simpan film favorit ke watchlist yang terikat pada akun masing-masing. Akun *guest* diarahkan untuk login terlebih dahulu sebelum menyimpan.
- **Estetika Material Design 3:** Tema gelap sinematik, *collapsing top bar* saat *scroll*, *shimmer skeleton* saat memuat, dan *Splash Screen* berlogo.

## 📱 Screenshot Terkini
| Home | Detail & Trailer | Profile |
| :---: | :---: | :---: |
| <img src="assets/img-home.jpg" width="230"> | <img src="assets/img-detail.jpg" width="230"> | <img src="assets/img-profile.jpg" width="230"> |

> Tambahkan gambar screenshot ke folder `assets/` sesuai nama di atas.

## 📥 Download APK
Anda dapat melihat seluruh versi aplikasi yang pernah dirilis melalui halaman **GitHub Releases**, sekaligus mengunduh versi terbaru dengan mudah.

🔗 **[Lihat Semua Versi Rilis](https://github.com/itozt/FP-PPB/releases)**

Untuk mengunduh **APK stabil terbaru**, silakan klik tombol di bawah ini:

⬇️ **[Download APK Terbaru](#download-badge)**

## ⚙️ Petunjuk Instalasi
1. Unduh berkas APK dari tautan di atas.
2. Buka paket installer yang terunduh pada perangkat Android Anda.
3. Apabila muncul peringatan keamanan, izinkan pemasangan dari **Sumber Tidak Dikenal (Unknown Sources)** pada pengaturan keamanan ponsel.
4. Ikuti instruksi di layar hingga selesai, dan aplikasi siap digunakan.

> **Catatan:** Aplikasi memerlukan koneksi internet untuk mengambil data film dari TMDB. Pemutaran trailer paling optimal pada perangkat dengan layanan Google.

## 🛠️ Stack Teknologi & Arsitektur
Dibangun mengikuti pola arsitektur **MVVM (Model-View-ViewModel)** dengan prinsip *Single Source of Truth* pada lapisan Repository:
- **Bahasa:** Kotlin
- **UI & Layouting:** Jetpack Compose (Material 3)
- **Reactivity & Threading:** Kotlin Coroutines beserta StateFlow
- **Networking:** Retrofit + OkHttp + Gson (sumber data **TMDB API**)
- **Lapisan Penyimpanan:** Room Database (SQLite) untuk watchlist & akun, serta SharedPreferences untuk *session*
- **Lainnya:** Coil (image loading), Navigation Compose, WebView (YouTube *iframe embed*)
- **Minimum SDK:** Android 7.0 (API 24)

## 📋 Changelog / Catatan Rilis
### v1.0.0 (Rilis Publik)
- Penjelajahan film berdasarkan kategori: Trending, Now Playing, Popular, dan Top Rated.
- *Hero slider* otomatis dengan *infinite scroll* di halaman Home.
- Pencarian film *debounced* dengan filter genre.
- Halaman detail film lengkap dengan pemutaran trailer YouTube *full-screen*.
- Autentikasi lokal: register, login email & password, serta mode Guest.
- Watchlist per-akun yang tersimpan secara offline.
- Halaman Profile menampilkan nama, email, dan daftar watchlist.
- Navigasi *swipe* antar tab, *collapsing top bar*, dan tema gelap Material 3.
