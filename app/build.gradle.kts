import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.gtp.showapicturetoyourfriend"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.gtp.showapicturetoyourfriend"
        minSdk = 26
        targetSdk = 36
        versionCode = 8
        versionName = "2.0.2"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}

if (project.hasProperty("sign")) {
    android {
        signingConfigs {
            create("release") {
                val properties = Properties()
                properties.load(project.rootProject.file("local.properties").inputStream())

                storeFile = file(properties.getProperty("uploadsigning.file"))
                storePassword = properties.getProperty("uploadsigning.password")
                keyAlias = properties.getProperty("uploadsigning.key.alias")
                keyPassword = properties.getProperty("uploadsigning.key.password")
            }
        }
    }
    android.buildTypes.getByName("release").signingConfig =
        android.signingConfigs.getByName("release")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)
    implementation(libs.coil3.compose)
    implementation(libs.zoomable)
    implementation(libs.sangun.compose.video)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
