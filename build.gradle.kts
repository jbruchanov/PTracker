import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("org.jlleitschuh.gradle.ktlint")
    id("com.jibru.kostra.resources")
}

apply(plugin = "com.jibru.kostra.resources")

group = "com.scurab"
version = "0.1"

dependencies {
    implementation(compose.desktop.currentOs)
    runtimeOnly(libs.kotlin.reflect)

    implementation(libs.bundles.kotlinx.coroutines)
    implementation(libs.bundles.icons)
    implementation(libs.bundles.kostra)
    implementation(libs.bundles.ktor)
    implementation(libs.kotlinx.datetime)
    implementation(libs.ooxml)
    implementation(libs.kotlinx.json)
    implementation(libs.koin)
    implementation(libs.logback)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testImplementation(libs.bundles.unittests.jvm)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = libs.versions.jvmtarget.get()
        freeCompilerArgs = freeCompilerArgs + listOf("-Xcontext-receivers")
    }
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.jvmtarget.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.jvmtarget.get())
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "PTracker"
            packageVersion = "1.0.0"
        }
    }
}

ktlint {
    //version.set("0.47.1")
    version.set("1.0.0")
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
}

kostra {
    kClassName.set("com.scurab.ptracker.K")
    androidResources {
        resourceDirs.add(file("src/main/resources_strings"))
    }
}
