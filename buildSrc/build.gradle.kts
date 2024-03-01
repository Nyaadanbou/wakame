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
    implementation(libs.indra.common)
    implementation(libs.kotlin.jvm)
    implementation(libs.ksp)
    implementation(libs.shadow)
}
