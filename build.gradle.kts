import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    id("org.jetbrains.compose") version "1.3.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

group = "com.scurab"
version = "0.1"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    val ktorVersion = "2.1.1"
    val logbackVersion = "1.4.1"
    val ktSerialization = "1.4.0"
    val junit = "5.9.0"
    val koinVersion = "3.2.2"
    val composeVersion = "1.3.0"
    val mockKVersion = "1.12.8"
    val coroutines = "1.6.4"

    implementation(compose.desktop.currentOs)
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutines")

    implementation("org.jetbrains.compose.material:material-icons-core-desktop:$composeVersion")
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:$composeVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("org.apache.poi:poi-ooxml:5.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$ktSerialization")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$ktSerialization")

    implementation("io.insert-koin:koin-core:$koinVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("io.mockk:mockk:$mockKVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:$junit")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + listOf("-Xcontext-receivers")
    }
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
    version.set("0.45.2")
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
}
