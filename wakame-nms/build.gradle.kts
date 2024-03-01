plugins {
    id("neko-kotlin")
    id("neko.repositories") version "1.0"
    alias(libs.plugins.paperdev)
}

group = "cc.mewcraft.wakame"
version = "1.0.0"
description = "Add custom stuff to server"

dependencies {
    // server
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")

    // bytebuddy
    implementation(libs.bytebuddy)

    // helper
    compileOnly(libs.helper)

    // internal
    compileOnly(project(":wakame-common"))
    compileOnly(platform(libs.bom.adventure))
}

tasks {
    reobfJar {
        dependsOn(sourcesJar)
    }
}
