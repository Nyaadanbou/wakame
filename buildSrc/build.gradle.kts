plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    nyaadanbouPrivate()
}

dependencies {
    implementation(local.plugin.kotlin.jvm)
    implementation(local.plugin.libraries.repository)
    implementation(local.plugin.copy.jar.build)
    implementation(local.plugin.copy.jar.docker)
    implementation(local.plugin.paperweight.userdev)
    implementation(libs.shadow)
    implementation(libs.indra.common)
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(files(local.javaClass.superclass.protectionDomain.codeSource.location))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}