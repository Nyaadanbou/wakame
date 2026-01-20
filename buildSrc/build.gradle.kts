plugins {
    `kotlin-dsl`
}

repositories {
    // 为了导入 "nyaadanbou-repository-project"
    mavenLocal()

    // 常用公共仓库
    mavenCentral()

    // 官方 Gradle 插件仓库
    gradlePluginPortal()

    // 私有仓库
    nyaadanbouPrivate()
}

dependencies {
    implementation(local.plugin.kotlin.jvm)
    implementation(local.plugin.libraries.repository)
    implementation(local.plugin.nyaadanbou.repository)
    implementation(local.plugin.copy.jar.build)
    implementation(local.plugin.copy.jar.docker)
    implementation(local.plugin.blossom)
    implementation(local.plugin.shadow)
    implementation(local.plugin.gremlin.gradle)
    implementation(local.plugin.indra.common)
    implementation(local.plugin.paperweight.userdev)
}

dependencies {
    implementation(files(local.javaClass.superclass.protectionDomain.codeSource.location))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
