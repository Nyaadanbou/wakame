plugins {
    id("cc.mewcraft.repo-conventions")
    id("cc.mewcraft.kotlin-conventions")
    id("cc.mewcraft.koin-conventions")
}

group = "cc.mewcraft.wakame"
version = "1.0.0"
description = "Add custom stuff to server"

dependencies {
    // server
    compileOnly(libs.server.purpur)

    // helper
    compileOnly(libs.helper)
}
