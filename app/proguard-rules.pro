# --- TeamCaptain Notes ProGuard / R8 rules ---

# Keep Kotlin metadata.
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisible*Annotations, EnclosingMethod

# --- kotlinx.serialization ---
# Keep the generated serializers and companion serializer accessors.
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keep class <1> {
    static <1>$Companion Companion;
}
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep every model class (they are (de)serialized reflectively via generated code).
-keep,includedescriptorclasses class com.teamcaptain.notes.data.model.** { *; }
-keep,includedescriptorclasses class com.teamcaptain.notes.data.remote.**Dto { *; }
-keep,includedescriptorclasses class com.teamcaptain.notes.data.remote.dto.** { *; }

# --- Retrofit / OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Retrofit service interfaces.
-keep interface com.teamcaptain.notes.data.remote.FootballDataApiService { *; }

# Compose keeps itself via the AGP-bundled rules; nothing extra required.
