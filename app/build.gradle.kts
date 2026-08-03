import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
}

// --- Read local.properties (never committed) for the API token / base URL. ---
val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localPropsFile.inputStream().use { localProps.load(it) }
}

fun readProp(key: String, default: String): String {
    // Priority: environment variable (CI) -> local.properties -> default.
    return System.getenv(key) ?: localProps.getProperty(key) ?: default
}

val footballApiToken: String = readProp("FOOTBALL_DATA_API_TOKEN", "your_api_token_here")
val footballApiBaseUrl: String = readProp("FOOTBALL_API_BASE_URL", "https://api.football-data.org/v4")

// --- Optional release signing pulled from environment (GitHub Secrets). ---
// NOTE: these locals are intentionally NOT named keyAlias/keyPassword, because
// inside signingConfigs.create("release") { } those names would collide with the
// SigningConfig receiver's own properties and cause a silent self-assignment.
val envKeystorePath: String? = System.getenv("ANDROID_KEYSTORE_PATH")
val envStorePassword: String? = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val envKeyAlias: String? = System.getenv("ANDROID_KEY_ALIAS")
val envKeyPassword: String? = System.getenv("ANDROID_KEY_PASSWORD")
// The key password defaults to the store password (same value per project policy).
val effectiveKeyPassword: String? = envKeyPassword?.ifBlank { null } ?: envStorePassword
val hasReleaseSigning =
    !envKeystorePath.isNullOrBlank() && file(envKeystorePath).exists() && !envStorePassword.isNullOrBlank()

android {
    namespace = "com.teamcaptain.notes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.teamcaptain.notes"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        // Exposed to code as BuildConfig.FOOTBALL_DATA_API_TOKEN / FOOTBALL_API_BASE_URL.
        buildConfigField("String", "FOOTBALL_DATA_API_TOKEN", "\"$footballApiToken\"")
        buildConfigField("String", "FOOTBALL_API_BASE_URL", "\"$footballApiBaseUrl\"")

        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(envKeystorePath!!)
                storePassword = envStorePassword
                keyAlias = envKeyAlias?.ifBlank { null } ?: "teamcaptain_notes_key"
                keyPassword = effectiveKeyPassword
                // PKCS12 keystore.
                storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            // Enabled only after a verified non-minified release build.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use the real release keystore when provided (CI / signed local build);
            // otherwise fall back to debug signing so a plain `assembleRelease`
            // still succeeds for local smoke tests.
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        // Support Android 15+ 16 KB memory page sizes (no legacy native libs bundled).
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.8.1")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // First-party kotlinx.serialization converter bundled with Retrofit 2.11.0.
    // Provides retrofit2.converter.kotlinx.serialization.asConverterFactory(MediaType).
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    debugImplementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
