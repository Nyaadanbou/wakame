plugins {
    id("neko-kotlin")
    id("nyaadanbou-conventions.repositories")
    alias(libs.plugins.paperdev)
}

group = "cc.mewcraft.wakame"
version = "1.0"
description = "The mixin part"

dependencies {
    paperweight.paperDevBundle(local.versions.paper)

    remapper("net.fabricmc", "tiny-remapper", "0.10.1", classifier = "fat")

    compileOnly(local.ignite)
    compileOnly(local.mixin)
    compileOnly(local.mixin.extras)

    compileOnly(project(":wakame-common"))
}

paperweight {
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}