plugins {
    id("cc.mewcraft.repo-conventions")
    id("cc.mewcraft.kotlin-conventions")
}

group = "cc.mewcraft.wakame"
version = "1.0.0"
description = "Add custom stuff to server"

dependencies {
    // server
    compileOnly(libs.server.paper)

    // helper
    compileOnly(libs.helper)
}
