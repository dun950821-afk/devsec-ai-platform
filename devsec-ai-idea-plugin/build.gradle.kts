import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginStructureTask

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.10"
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
        local("D:/Program Files/JetBrains/IntelliJ IDEA 2026.1.3")
        bundledPlugin("com.intellij.java")
        bundledPlugin("Git4Idea")
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
        name = "DevSecAI Security Assistant"
        version = "1.0.0"
        description = """
            <p><b>DevSecAI Security Assistant for IntelliJ IDEA.</b></p>
            <p>DevSecAI 安全开发助手用于在 IntelliJ IDEA 中进行项目安全自查，并与 DevSecAI 插件管理平台联动。</p>
            <ul>
              <li>在设置页配置平台地址、访问令牌和插件实例 ID。</li>
              <li>在 DevSecAI 工具窗口执行 SAST、Secrets、SCA 检测并查看风险列表。</li>
              <li>将检测结果上传到管理平台，便于统一查看和治理。</li>
              <li>提交代码前按后台策略进行安全检查，可阻断严重或高危风险。</li>
            </ul>
            """.trimIndent()
        ideaVersion {
            sinceBuild = "242"
            // No untilBuild: do not artificially block future IDEA versions.
        }
    }
    pluginVerification {
        ides {
            local(file("D:/Program Files/JetBrains/IntelliJ IDEA 2026.1.3"))
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    named<VerifyPluginStructureTask>("verifyPluginStructure") {
        // 插件面向中文用户交付，保留中文描述，同时继续执行其余结构检查。
        ignoreUnacceptableWarnings.set(true)
    }
}
