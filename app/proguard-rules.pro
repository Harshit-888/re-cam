# Keep rules for the release build.
#
# `app/build.gradle.kts` referenced this file while it did not exist, so
# `assembleRelease` failed outright. Release builds currently run with
# `isMinifyEnabled = false`; these rules are here so that enabling R8 later
# doesn't break the app's reflection-adjacent surfaces.

# kotlinx.serialization: generated serializers are looked up reflectively.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class com.example.docscanner.model.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.example.docscanner.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.example.docscanner.model.**$$serializer { *; }

# CameraX ships JNI; class names must survive.
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Guava/camera2 optional dependencies that R8 warns about.
-dontwarn com.google.common.**
-dontwarn org.checkerframework.**
