plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.chatclientforgemini"
    compileSdk = 35                      // API 35

    defaultConfig {
        applicationId = "com.example.geminiwatchchat"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }

    buildFeatures {
        compose = true
    }
}

dependencies {
    /* ---------- version-catalog libraries ---------- */
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.activity.compose)          // rememberLauncherForActivityResult
    implementation(libs.wear.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.material3.android)

    /* ---------- Wear speech-to-text helper ---------- */
    implementation("androidx.wear:wear-input:1.1.0")   // RemoteInputIntentHelper

    /* ---------- Compose Material (buttons, icons) --- */
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")

    /* ---------- App-specific libraries -------------- */
    implementation("com.google.ai.client.generativeai:generativeai:0.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.wear:wear-remote-interactions:1.0.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.wear.compose:compose-material:1.5.0-beta05")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.security:security-crypto:1.1.0")
}