/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.mikepenz.aboutlibraries.plugin")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.blazecode.tsviewer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.blazecode.tsviewer"
        minSdk = 26
        targetSdk = 34
        versionCode = 10
        versionName = "2.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    flavorDimensions += "version"
    productFlavors {
        create("core") {
            dimension = "version"
            versionNameSuffix = "-core"
        }
        create("foss") {
            dimension = "version"
            versionNameSuffix = "-foss"
        }
    }
    sourceSets {
        getByName("core") {
            java.setSrcDirs(listOf("src/main/java", "src/core/java"))
        }
        getByName("foss") {
            java.setSrcDirs(listOf("src/main/java", "src/foss/java"))
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
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.4"
    }
}

val aboutLibrariesVersion: String by rootProject.extra
val composeVersion: String by rootProject.extra
val wearComposeVersion: String by rootProject.extra

dependencies {
    implementation(platform("androidx.compose:compose-bom:2023.10.00")) //https://mvnrepository.com/artifact/androidx.compose/compose-bom?repo=google

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation(project(mapOf("path" to ":wear")))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // NAVIGATION
    implementation("androidx.navigation:navigation-compose")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.33.2-alpha")

    // TRANSPARENT STATUS BAR
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.33.2-alpha")

    //COROUTINES
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    "coreImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    //WORK MANAGER
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    //ENCRYPTED SHARED PREFERENCES
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    //TS API
    implementation("com.github.theholywaffle:teamspeak3-api:1.3.1")

    //ABOUT LIBRARIES
    implementation("com.mikepenz:aboutlibraries:$aboutLibrariesVersion")
    implementation("com.mikepenz:aboutlibraries-compose:${aboutLibrariesVersion}")

    //ROOM DB
    val room_version = "2.5.2"
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // CHARTS
    implementation("com.patrykandpatrick.vico:core:1.7.1")
    implementation("com.patrykandpatrick.vico:compose:1.7.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.7.1")

    //LOTTIE ANIMATIONS
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    //VOLLEY FOR HTTP REQUESTS TO GITHUB FOR UPDATING
    implementation("com.android.volley:volley:1.2.1")

    //GSON FOR PARSING JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // COMPOSE PERMISSIONS
    implementation("com.google.accompanist:accompanist-permissions:0.30.1")

    //LOGGING
    implementation("com.jakewharton.timber:timber:5.0.1")

    //WEAR INTEGRATION
    project(":wear")
    "coreImplementation"("com.google.android.gms:play-services-wearable:18.1.0")

    // CRASH DETECTION
    val acraVersion = "5.11.2"
    implementation("ch.acra:acra-mail:$acraVersion")
    implementation("ch.acra:acra-dialog:$acraVersion")
    implementation("com.google.guava:guava:32.1.3-jre")
}