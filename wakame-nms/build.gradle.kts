import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("nyaadanbou-conventions.repositories")
    id("wakame-conventions.kotlin")
    alias(libs.plugins.paperdev)
}

version = "0.0.1"

dependencies {
    // server
    paperweight.paperDevBundle(local.versions.paper)

    // internal
    compileOnly(project(":wakame-common"))
    compileOnly(local.shadow.nbt)
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