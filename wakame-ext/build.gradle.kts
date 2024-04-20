plugins {
    id("neko.repositories") version "1.1.1-SNAPSHOT"
    id("neko-kotlin")
}

group = "cc.mewcraft.wakame"
version = "1.0.0"
description = "Add custom stuff to server"

dependencies {
    // server
    compileOnly(libs.server.purpur)

    // helper
    compileOnly(libs.helper)

    // internal
    compileOnly(platform(libs.bom.caffeine))
    compileOnly(project(":wakame-common"))

    // external
    compileOnly(libs.jgit)
    compileOnly(libs.mythicmobs) {
        isTransitive = false
    }
}
