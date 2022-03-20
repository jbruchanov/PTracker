import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("org.jetbrains.compose") version "1.2.0-alpha01-dev620"
}

group = "com.scurab"
version = "0.1"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    val ktorVersion = "1.6.7"
    val logbackVersion = "1.2.3"
    val ktSerialization = "1.3.2"
    val junit = "5.8.2"
    val koinVersion = "3.1.5"
    val composeVersion = "1.2.0-alpha01-dev620"

    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material:material-icons-core-desktop:$composeVersion")
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:$composeVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
    implementation("org.apache.poi:poi-ooxml:5.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$ktSerialization")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$ktSerialization")

    implementation("io.insert-koin:koin-core:$koinVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junit")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
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