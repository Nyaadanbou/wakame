import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("neko-kotlin")
    id("neko.repositories") version "1.0-SNAPSHOT"
    alias(libs.plugins.paperdev)
}

group = "cc.mewcraft.wakame"
version = "1.0.0"
description = "Add custom stuff to server"

dependencies {
    // server
    paperweight.paperDevBundle("1.20.5-R0.1-SNAPSHOT")

    // helper
    compileOnly(libs.helper)

    // internal
    compileOnly(project(":wakame-common"))
    compileOnly(platform(libs.bom.adventure))
    compileOnly(libs.bytebuddy)
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
}
