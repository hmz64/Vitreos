# Add project specific ProGuard rules here.
-keepattributes *Annotation*

# Socket.io
-keep class io.socket.** { *; }
-dontwarn io.socket.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# JSON
-keep class org.json.** { *; }
-dontwarn org.json.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}