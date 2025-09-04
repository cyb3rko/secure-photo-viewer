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
        minSdk = 23
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
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("com.github.bumptech.glide:glide:5.0.4")
    implementation("com.github.MikeOrtiz:TouchImageView:3.6")
    //implementation 'io.github.kobakei:ratethisapp:1.2.0'
}
