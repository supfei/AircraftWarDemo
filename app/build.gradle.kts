import java.io.File

plugins {
    alias(libs.plugins.android.application)
}

// 从项目根目录 .env 读取键值对（忽略空行与 # 注释）。
fun loadDotEnv(envFile: File): Map<String, String> {
    if (!envFile.exists()) return emptyMap()
    val map = mutableMapOf<String, String>()
    envFile.readLines().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach
        val idx = trimmed.indexOf('=')
        if (idx <= 0) return@forEach
        val key = trimmed.substring(0, idx).trim()
        var value = trimmed.substring(idx + 1).trim()
        if (value.length >= 2 &&
            ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'")))
        ) {
            value = value.substring(1, value.length - 1)
        }
        map[key] = value
    }
    return map
}

val dotEnv: Map<String, String> = loadDotEnv(rootProject.file(".env"))
val publicIp: String = dotEnv["PUBLIC_IP"] ?: "127.0.0.1"
val scoreApiPort: String = dotEnv["SCORE_API_PORT"] ?: "8080"
val scoreApiBaseUrl: String =
    dotEnv["SCORE_API_BASE_URL"]?.trim()?.takeIf { it.isNotEmpty() }
        ?: "http://${publicIp}:${scoreApiPort}/"

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

        // 将 .env 中的服务地址注入 BuildConfig，供 Java 读取。
        buildConfigField("String", "SCORE_API_BASE_URL", "\"${scoreApiBaseUrl.replace("\"", "\\\"")}\"")
    }

    buildFeatures {
        buildConfig = true
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