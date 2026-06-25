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
        intellijIdeaUltimate("2026.1.3")
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
            // No untilBuild — do not artificially block future IDEA versions
        }
    }
    verifyPlugin {
        ides {
            // Only verify against the target platform
            ide("2026.1.3")
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
}
