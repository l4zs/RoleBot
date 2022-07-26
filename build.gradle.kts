import java.util.Locale

plugins {
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("dev.schlaubi.mikbot.gradle-plugin") version "2.5.0"
}

group = "de.l4zs"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    mikbot("dev.schlaubi", "mikbot-api", "3.8.0-SNAPSHOT")
    ksp("dev.schlaubi", "mikbot-plugin-processor", "2.2.1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "18"
    }

    task<Copy>("buildAndInstall") {
        group = "build"
        dependsOn(assemblePlugin)
        from(assemblePlugin)
        include("*.zip")
        into("plugins")
    }

    val generateDefaultResourceBundle = task<dev.schlaubi.mikbot.gradle.GenerateDefaultTranslationBundleTask>("generateDefaultResourceBundle") {
        defaultLocale.set(Locale("en", "GB"))
    }

    assemblePlugin {
        dependsOn(generateDefaultResourceBundle)
    }

    assembleBot {

    }

    installBot {
        botVersion.set("3.8.0-SNAPSHOT")
    }

    pluginPublishing {
        // The address your repository is hosted it
        // if you use Git LFS and GitHub Pages use https://github.com/owner/repo/raw/branch
        repositoryUrl.set("https://github.com/l4zs/RoleBotRewrite/raw/main")
        // The directory the generated repository should be in
        targetDirectory.set(rootProject.file("ci-repo").toPath())
        // The URL of the project
        projectUrl.set("https://github.com/l4zs/RoleBotRewrite")
    }
}

mikbotPlugin {
    description.set("RoleBot")
    provider.set("l4zs")
    license.set("GPLv3")
}
