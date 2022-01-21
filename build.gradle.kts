import dev.schlaubi.mikbot.gradle.GenerateDefaultTranslationBundleTask
import java.util.Locale
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.google.devtools.ksp") version "1.6.0-1.0.1"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    id("dev.schlaubi.mikbot.gradle-plugin") version "1.3.2"
}

ktlint {
    disabledRules.set(listOf("no-wildcard-imports"))
}

group = "de.l4zs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://schlaubi.jfrog.io/artifactory/mikbot/")
    maven("https://schlaubi.jfrog.io/artifactory/envconf/")
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
}

dependencies {
    // this one is included in the bot itself, therefore we make it compileOnly
    compileOnly(kotlin("stdlib-jdk8"))
    compileOnly("dev.schlaubi", "mikbot-api", "2.0.1")
    ksp("dev.schlaubi", "mikbot-plugin-processor", "1.0.0")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn", "-Xopt-in=kotlin.time.ExperimentalTime", "-Xopt-in=io.ktor.locations.KtorExperimentalLocationsAPI")
        }
    }

    task<Copy>("buildAndInstall") {
        group = "build"
        dependsOn(assemblePlugin)
        from(assemblePlugin)
        include("*.zip")
        into("plugins")
    }

    val generateDefaultResourceBundle = task<GenerateDefaultTranslationBundleTask>("generateDefaultResourceBundle") {
        defaultLocale.set(Locale("en", "GB"))
    }

    assemblePlugin {
        dependsOn(generateDefaultResourceBundle)
    }

    installBot {
        botVersion.set("2.1.0-SNAPSHOT-SNAPSHOT")
    }

    buildRepository {
        repositoryUrl.set("https://role-bot-repo.l4zs.de")
        targetDirectory.set(rootProject.file("ci-repo").toPath())
        projectUrl.set("https://github.com/l4zs/rolebot")
    }
}

mikbotPlugin {
    provider.set("l4zs")
    license.set("l4zs forogot to make a LICENSE file (idiot)")
    description.set("l4zs' cool self-hosted role-bot.")
}
