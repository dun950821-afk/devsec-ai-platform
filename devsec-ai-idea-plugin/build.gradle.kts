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

// ============================================================
// Option A: Use local IDEA installation (no download needed)
//   Set ideaInstallationPath in gradle.properties, e.g.:
//     ideaInstallationPath=D:\\Program Files\\JetBrains\\IntelliJ IDEA 2026.1.3
//   Or pass via command line:
//     -PideaInstallationPath="D:/Program Files/JetBrains/IntelliJ IDEA 2026.1.3"
//
// Option B: Download from remote (default, requires ~1GB download)
//   Comment out or remove the ideaInstallationPath property
// ============================================================

val localIdeaPath: String? = providers.gradleProperty("ideaInstallationPath").orNull

dependencies {
    intellijPlatform {
        if (localIdeaPath != null) {
            local(localIdeaPath)
        } else {
            intellijIdeaUltimate("2026.1.3")
        }
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
