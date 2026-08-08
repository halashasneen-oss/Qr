-keep class com.halashasneen.nova.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn com.google.zxing.**
-keep class com.google.mlkit.** { *; }

# AdMob R8 missing classes fix
-dontwarn android.media.LoudnessCodecController$OnLoudnessCodecUpdateListener
-dontwarn android.media.LoudnessCodecController
