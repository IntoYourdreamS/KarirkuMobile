plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.tem2.karirku"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tem2.karirku"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    // ✅ aktifkan viewBinding agar gampang akses layout
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // 🔹 Komponen dasar Android
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // 🔹 HTTP client untuk request API Supabase / upload
    implementation("com.android.volley:volley:1.2.1")

    // 🔹 Parsing file PDF ke teks
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    // 🔹 JSON parser
    implementation("com.google.code.gson:gson:2.11.0")

    // 🔹 Logging (buat debugging)
    implementation("com.jakewharton.timber:timber:5.0.1")

    // 🔹 (Opsional) Supabase SDK — boleh dihapus kalau error di Java-only project
    // implementation("io.supabase:supabase-kt:2.3.1")

    // 🔹 Coroutine (untuk proses background parsing, optional)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ✅ ML Kit Text Recognition untuk OCR (Scan CV dengan Kamera)
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")

    // ✅ (Opsional) CameraX - jika mau upgrade ke camera yang lebih advanced
    // def camerax_version = "1.3.0"
    // implementation("androidx.camera:camera-camera2:${camerax_version}")
    // implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    // implementation("androidx.camera:camera-view:${camerax_version}")

    // 🔹 Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}