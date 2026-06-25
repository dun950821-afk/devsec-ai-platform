import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "com.guoshun"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaUltimate("2024.2")
        testFramework(TestFrameworkType.Platform)
        pluginVerifier()
        zipSigner()
    }
    implementation("com.google.code.gson:gson:2.10.1")
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        id = "com.guoshun.devsecai"
        name = "DevSecAI - Security Assistant"
        version = "1.0.0"
        description = """
            DevSecAI Security Assistant - Real-time security detection IDE plugin.
            SAST, Secrets Detection, Git Commit Check, AI Analysis.
            """
        ideaVersion {
            sinceBuild = "242"
            untilBuild = "261.*"
        }
    }
    verifyPlugin {
        ides {
            ide("2024.2")
            ide("2025.1")
            ide("2026.1")
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"
        }
    }
}
