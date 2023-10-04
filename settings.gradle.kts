pluginManagement {
    repositories {
        mavenLocal()
        google()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
    }
}

buildscript {
    repositories {
        mavenLocal()
        google()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.github.jbruchanov.kostra:com.jibru.kostra.resources.gradle.plugin:0.1.0")
    }
}

plugins {
    kotlin("jvm") version "1.9.10" apply false
    kotlin("plugin.serialization") version "1.9.10" apply false
    id("org.jetbrains.compose") version "1.5.1" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0" apply false
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
    }
}

rootProject.name = "PTracker"
