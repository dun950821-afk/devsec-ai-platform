import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "com.guoshun.devsecai"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure Gradle IntelliJ Platform Plugin 2.x
intellijPlatform {
    buildSearchableOptions = false

    instrumentCode = true

    verifyPlugin = false

    projectConventions {
        repository = true
    }
}

dependencies {
    // IntelliJ Platform SDK — 2026.1
    intellijIdeaUltimate("2026.1")
    intellijPlugins("com.intellij.java", "Git4Idea")

    implementation("com.google.code.gson:gson:2.10.1")
}

// Java 21 required for IDEA 2024.2+ / 2026.x
kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }

    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set("261.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
