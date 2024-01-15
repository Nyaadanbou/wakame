plugins {
    id("cc.mewcraft.repo-conventions")
    id("cc.mewcraft.kotlin-conventions")
    alias(libs.plugins.paperdev)
}

group = "cc.mewcraft.wakame"
version = "1.0.0"
description = "Add custom stuff to server"

dependencies {
    // server
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")

    // helper
    compileOnly(libs.helper)

    // internal
    compileOnly("net.kyori:adventure-nbt:4.14.0")
}

tasks {
    reobfJar {
        dependsOn(sourcesJar)
    }
}
