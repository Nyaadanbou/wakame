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
    paperweight.paperDevBundle(local.versions.paper)

    // helper
    compileOnly(local.helper)
    compileOnly(libs.shadow.nbt)

    // internal
    compileOnly(project(":wakame-common"))
    compileOnly(platform(libs.bom.adventure))
    compileOnly(platform(libs.bom.packetevents.spigot))
}

paperweight {
    reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION
}

tasks {
    // invui 依然使用 spigot-mapping; 我们必须暂时基于 spigot-mapping 构建 JAR
    assemble {
        dependsOn(reobfJar)
    }
}