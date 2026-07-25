<p align="center">
  <img name="download-badge" src="app/src/main/res/drawable/movflix_logo.png" alt="MovFlix Logo" width="250" style="background-color: black; padding: 40px 25%; border-radius: 12px;">
</p>

<h1 align="center">MovFlix</h1>

<p align="center">
  <a href="https://github.com/itozt/FP-PPB/releases/latest"><img src="https://img.shields.io/github/v/tag/itozt/FP-PPB?color=brightgreen&label=version&style=for-the-badge" alt="Latest Version"></a>
  &nbsp;
  <a href="https://github.com/itozt/FP-PPB/releases/latest/download/movflix_v1.1.0.apk"><img src="https://img.shields.io/badge/download_apk-blue?style=for-the-badge&logo=android" alt="Download APK"></a>
</p>

> Aplikasi katalog film modern untuk menjelajah, mencari, dan menonton trailer film terbaru, dibangun sepenuhnya menggunakan teknologi Jetpack Compose dengan data real-time dari TMDB.

## 📖 Tentang

**MovFlix** adalah aplikasi _movie catalogue_ yang menampilkan daftar film populer, detail film, serta pemutaran _trailer_ langsung di dalam aplikasi. Seluruh data film diambil secara _real-time_ dari **TMDB (The Movie Database) API**. Aplikasi ini menghadirkan navigasi yang responsif berbasis gestur _swipe_, dilengkapi sistem **autentikasi lokal** (login, register, dan guest) serta **watchlist** yang tersimpan terpisah untuk setiap akun secara _offline_ di perangkat. Dikembangkan sebagai pemenuhan evaluasi Final Project mata kuliah Pemrograman Perangkat Bergerak.

## ✨ Fitur Utama

- **Autentikasi Lokal:** Register dan login dengan email & password, atau **Masuk sebagai Guest** tanpa akun. Password diamankan dengan _hashing_ **SHA-256 + salt** dan disimpan secara lokal melalui Room.
- **Navigasi Cepat Berbasis Swipe:** Transisi mulus antara layar **Home**, **Search**, dan **Profile** menggunakan HorizontalPager yang tersinkron dengan _bottom navigation_.
- **Beranda Dinamis:** _Hero slider_ film _trending_ yang bergeser otomatis dan tak terbatas (_infinite loop_), diikuti baris kategori **Now Playing**, **Popular**, dan **Top Rated**.
- **Pencarian Cerdas:** Pencarian film secara _debounced_ (tanpa tombol submit) lengkap dengan filter **genre**.
- **Detail & Trailer:** Halaman detail film menampilkan sinopsis, rating, durasi, dan genre, serta pemutaran **trailer YouTube** _full-screen_ langsung di dalam aplikasi.
- **Watchlist Per-Akun:** Simpan film favorit ke watchlist yang terikat pada akun masing-masing. Akun _guest_ diarahkan untuk login terlebih dahulu sebelum menyimpan.
- **Estetika Material Design 3:** Tema gelap sinematik, _collapsing top bar_ saat _scroll_, _shimmer skeleton_ saat memuat, dan _Splash Screen_ berlogo.

## 📱 Screenshot Terkini

|                                                                      Home                                                                       |                                                                  Search                                                                   |                                                                  Detail                                                                   |                                                                    Profile                                                                    |
| :---------------------------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------------: | :-------------------------------------------------------------------------------------------------------------------------------------------: |
| <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/585f6d27-eb5c-4ed7-b3b6-8327a231f6cd" /> | <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/b5bafa1e-5f95-4f0c-a0df-43d52a4f38bd" /> | <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/3f1bab4e-8c38-4c78-af54-5ba2d9e8466f" /> | <img width="1080" height="2400" alt="image" src="https://github.com/user-attachments/assets/88778abf-1109-48d7-bcf8-bd9f5f498af7" />
 |

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

Dibangun mengikuti pola arsitektur **MVVM (Model-View-ViewModel)** dengan prinsip _Single Source of Truth_ pada lapisan Repository:

- **Bahasa:** Kotlin
- **UI & Layouting:** Jetpack Compose (Material 3)
- **Reactivity & Threading:** Kotlin Coroutines beserta StateFlow
- **Networking:** Retrofit + OkHttp + Gson (sumber data **TMDB API**)
- **Lapisan Penyimpanan:** Room Database (SQLite) untuk watchlist & akun, serta SharedPreferences untuk _session_
- **Lainnya:** Coil (image loading), Navigation Compose, WebView (YouTube _iframe embed_)
- **Minimum SDK:** Android 7.0 (API 24)

## 📋 Changelog / Catatan Rilis

### v1.1.0 (Latest)

- Pembaruan antarmuka (UI) dan tata letak untuk pengalaman pengguna yang lebih rapi dan nyaman.
- Pencarian kini lebih pintar: otomatis menghapus filter saat kolom pencarian dikosongkan.
- Menyaring hasil pencarian agar hanya menampilkan film dengan data dan gambar yang valid.
- Memperbaiki animasi geser otomatis pada _banner_ halaman utama.

### v1.0.0

- Penjelajahan film berdasarkan kategori: Trending, Now Playing, Popular, dan Top Rated.
- _Hero slider_ otomatis dengan _infinite scroll_ di halaman Home.
- Pencarian film _debounced_ dengan filter genre.
- Halaman detail film lengkap dengan pemutaran trailer YouTube _full-screen_.
- Autentikasi lokal: register, login email & password, serta mode Guest.
- Watchlist per-akun yang tersimpan secara offline.
- Halaman Profile menampilkan nama, email, dan daftar watchlist.
- Navigasi _swipe_ antar tab, _collapsing top bar_, dan tema gelap Material 3.
