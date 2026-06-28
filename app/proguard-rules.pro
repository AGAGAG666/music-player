# ProGuard rules for MusicPlayer

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.music.player.data.model.** { *; }
-keep class retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }

# ExoPlayer
-keep class com.google.android.exoplayer2.** { *; }
