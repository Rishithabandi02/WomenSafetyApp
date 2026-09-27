# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep annotations
-keepattributes *Annotation*

# Google Maps
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }

# Suppress harmless warnings
-dontwarn org.intellij.lang.annotations.**
-dontwarn javax.annotation.**