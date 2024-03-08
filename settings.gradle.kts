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
        classpath("com.github.jbruchanov.kostra:com.jibru.kostra.resources.gradle.plugin:0.1.3")
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.60.5"
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.serialization") version "1.9.22" apply false
    id("org.jetbrains.compose") version "1.6.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.0.2" apply false
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
