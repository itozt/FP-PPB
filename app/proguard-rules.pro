# Add project specific ProGuard rules here.

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclassmembernames,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# DTOs and domain models — preserve field names for Gson deserialization
-keep class com.example.moviecatalogue.data.remote.** { *; }
-keep class com.example.moviecatalogue.domain.** { *; }
-keep class com.example.moviecatalogue.data.local.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# YouTube Player
-keep class com.pierfrancescosoffritti.androidyoutubeplayer.** { *; }

# Coil
-dontwarn coil.**
