# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}

# Gson - keep our data models
-keep class com.amweather.amweather.data.** { *; }

# OkHttp
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep WorkManager worker classes (instantiated by name via reflection)
-keep class com.amweather.amweather.worker.** { *; }

# Keep WorkManager internal components intact
-keep class androidx.work.impl.background.systemalarm.SystemAlarmService { *; }
-keep class androidx.work.impl.background.systemjob.SystemJobService { *; }
-keep class androidx.work.impl.foreground.SystemForegroundService { *; }
-keep class androidx.work.impl.workers.DiagnosticsWorker { *; }

# Keep Room database implementations used by WorkManager
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.work.impl.**

# Preserve generic signatures so TypeToken can read them at runtime
-keepattributes Signature

# Maintain Gson's specific annotation and internal handling
-keepattributes *Annotation*
-dontwarn sun.misc.**

# Keep Gson's underlying type-handling classes
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements java.lang.reflect.Type { *; }

# Keep your data models intact so Gson can map JSON fields to them
-keep class com.amweather.amweather.data.** { *; }
