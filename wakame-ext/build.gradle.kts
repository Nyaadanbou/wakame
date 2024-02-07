plugins {
    id("cc.mewcraft.repo-conventions")
    id("cc.mewcraft.kotlin-conventions")
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
    compileOnly(libs.caffeine)
    compileOnly(project(":wakame:wakame-common"))

    // external
    compileOnly(libs.mythicmobs) { isTransitive = false }
}
