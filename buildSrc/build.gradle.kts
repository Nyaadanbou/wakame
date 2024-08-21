plugins {
    `kotlin-dsl`
}

group = "cc.mewcraft.wakame"
version = "1.0.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(local.kotlin.jvm)
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