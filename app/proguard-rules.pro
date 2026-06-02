# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment to preserve line numbers in crash stack traces.
#-keepattributes SourceFile,LineNumberTable
#-renamesourcefileattribute SourceFile

# Retrofit — kept broad intentionally: Retrofit uses dynamic Proxy to create service
# implementations; its own consumer rules handle annotation retention, but a past
# obfuscation breakage showed R8 could still strip internals needed at runtime.
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# OkHttp — same reasoning as Retrofit above.
-keep class okhttp3.** { *; }
-dontwarn okio.**

# Gson — keep all data models so field names survive for JSON mapping.
# Also keep TypeToken subclasses used for generic deserialization in
# SettingsRepository and WeatherCache.
-keep class com.amweather.amweather.data.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-dontwarn sun.misc.**

# WorkManager — worker classes are instantiated by class name via reflection.
# WorkManager's own consumer rules already cover ListenableWorker subclasses,
# but keeping the package explicitly is clearer and harmless.
-keep class com.amweather.amweather.worker.** { *; }

# WorkManager internal services — not in WorkManager's consumer rules;
# kept explicitly as these are looked up by the WorkManager runtime.
-keep class androidx.work.impl.background.systemalarm.SystemAlarmService { *; }
-keep class androidx.work.impl.background.systemjob.SystemJobService { *; }
-keep class androidx.work.impl.foreground.SystemForegroundService { *; }
-keep class androidx.work.impl.workers.DiagnosticsWorker { *; }

# WorkManager uses Room internally for its job queue — WorkDatabase extends RoomDatabase
# and is instantiated by name at runtime.
-keep class * extends androidx.room.RoomDatabase { *; }
