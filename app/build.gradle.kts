plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.aircraftwardemo"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.aircraftwardemo"
        minSdk = 30
        targetSdk = 36
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // 网络库依赖
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)  // 可选，用于调试
    implementation(libs.gson)
    implementation(libs.moshi)  // 可选，如果你用Moshi而不是Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

}