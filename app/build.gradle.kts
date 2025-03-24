

import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    id("kotlin-parcelize")
    id ("kotlin-kapt")

}

android {
    namespace = "com.example.myblog"
    compileSdk = 35
    buildFeatures {
        buildConfig = true
    }

        defaultConfig {
        applicationId = "com.example.myblog"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "CLOUD_NAME", "\"${project.properties["CLOUD_NAME"] ?:""}\"")
        buildConfigField("String", "API_KEY", "\"${project.properties["API_KEY"] ?:""}\"")
        buildConfigField("String", "API_SECRET", "\"${project.properties["API_SECRET"] ?:""}\"")
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"${project.properties["DEEPSEEK_API_KEY"] ?:""}\"")


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

    buildFeatures {
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }


}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.glide)
    implementation (libs.cloudinary.android)
    implementation(libs.firebase.firestore)

    implementation(libs.generativeai)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.room.ktx)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.androidx.room.runtime)
    implementation(libs.androidx.room.common)
    kapt(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}