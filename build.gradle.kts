/*
 *
 *  * Copyright (c) BlazeCode / Ralf Lehmann, 2022.
 *
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val aboutLibrariesVersion by extra("11.2.3")
    val composeVersion by extra("1.7.4")
    val wearComposeVersion by extra("1.3.0-alpha07")

    repositories {
        google()
        mavenCentral()
        //NEEDED FOR TOOLTIPS
        maven { url = uri("https://jitpack.io") }
        //NEEDED FOR ABOUTLIBRARIES
        maven { url = uri("https://plugins.gradle.org/m2/")}
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.6.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")

        //ABOUT LIBRARIES
        classpath("com.mikepenz.aboutlibraries.plugin:aboutlibraries-plugin:$aboutLibrariesVersion")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven { url = uri("https://jitpack.io") }
    }
}

plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.26" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}