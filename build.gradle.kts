plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    application
}

ktlint {
    disabledRules.set(listOf("no-wildcard-imports"))
}

group = "de.l4zs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
    maven("https://schlaubi.jfrog.io/artifactory/envconf/")
}

dependencies {
    // Bot
    implementation("com.kotlindiscord.kord.extensions", "kord-extensions", "1.5.0-SNAPSHOT")
    implementation("dev.kord.x", "emoji", "0.5.0")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", "1.5.2")
    implementation("org.litote.kmongo", "kmongo-coroutine-serialization", "4.3.0")

    // Logging
    implementation("ch.qos.logback", "logback-classic", "1.2.6")

    // Util
    implementation("dev.schlaubi", "envconf", "1.1")
}

application {
    mainClassName = "de.l4zs.rolebot.LauncherKt"
    mainClass.set("de.l4zs.rolebot.LauncherKt")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(16))
    }
}

tasks {
    jar {
        enabled = false
    }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "16"
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xopt-in=kotlin.time.ExperimentalTime", "-Xopt-in=io.ktor.locations.KtorExperimentalLocationsAPI")
        }
    }
}
