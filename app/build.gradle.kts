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
        signingConfig = signingConfigs.getByName("debug")
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    bundle {
        storeArchive {
            enable = false
        }
    }
    packaging {
        resources {
            excludes.add("META-INF/*.version")
            excludes.add("META-INF/**/LICENSE.txt")
        }
    }
    dependenciesInfo {
        // Disable including dependency metadata when building APKs
        includeInApk = false
        // Disable including dependency metadata when building Android App Bundles
        includeInBundle = false
    }
}

// Automatic pipeline build
// build with '-Psign assembleRelease'
// output at 'app/build/outputs/apk/release/app-release.apk'
// build with '-Psign bundleRelease'
// output at 'app/build/outputs/bundle/release/app-release.aab'
if (project.hasProperty("sign")) {
    android {
        signingConfigs {
            create("release") {
                enableV3Signing = true
                enableV4Signing = true
                storeFile = file(System.getenv("KEYSTORE_FILE"))
                storePassword = System.getenv("KEYSTORE_PASSWD")
                keyAlias = System.getenv("KEYSTORE_KEY_ALIAS")
                keyPassword = System.getenv("KEYSTORE_KEY_PASSWD")
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
