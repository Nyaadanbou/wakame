plugins {
    id("nyaadanbou-conventions.repositories")
    id("wakame-conventions.kotlin")
    id("cc.mewcraft.build-copy")
    id("cc.mewcraft.docker-copy")
    id("io.papermc.paperweight.userdev")
    alias(local.plugins.blossom)
}

group = "cc.mewcraft.wakame"
version = "0.0.1-snapshot"
description = "The core plugin of Nyaadanbou"

dependencies {
    // internal
    compileOnlyApi(project(":wakame-api")) // 运行时由 wakame-mixin 提供
    compileOnlyApi(project(":wakame-common")) // 同上
    compileOnly(project(":wakame-mixin"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-adventurelevel"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-chestsort"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-economy"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-luckperms"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-mythicmobs"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-towny"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-townyflight"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-vault"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-worldguard"))

    // libraries
    paperweight.paperDevBundle(local.versions.paper)
    compileOnlyApi(platform(local.koin.bom))
    compileOnlyApi(local.koin.core)
    implementation(platform(local.koin.bom))
    implementation(local.koin.core)
    implementation(local.commons.collections)
    implementation(local.commons.gson)
    implementation(local.commons.provider)
    implementation(local.commons.reflection)
    implementation(local.commons.tuple)
    implementation(local.fleks) {
        exclude("org.jetbrains")
    }
    implementation(libs.mocha)
    implementation(local.snakeyaml.engine)
    implementation(platform(libs.bom.adventure))
    compileOnlyApi(platform(libs.bom.caffeine))
    implementation(platform(libs.bom.caffeine))
    implementation(platform(libs.bom.configurate.yaml))
    implementation(platform(libs.bom.configurate.gson))
    implementation(platform(libs.bom.configurate.extra.kotlin))
    implementation(platform(libs.bom.configurate.extra.dfu4))
    implementation(platform(libs.bom.creative))
    implementation(platform(libs.bom.cloud.paper))
    implementation(platform(libs.bom.cloud.kotlin))
    compileOnly(platform(libs.bom.invui)) { // 由自定义的 classloader 加载
        exclude("org.jetbrains")
    }
    implementation(platform(libs.bom.jgit))

    // other plugins (hard dependencies)
    compileOnly(local.adventurelevel)

    // test
    testImplementation(project(":wakame-api"))
    testImplementation(project(":wakame-common"))
    testImplementation(libs.logback.classic)
    testImplementation(libs.mockk)
    testImplementation(local.koin.test.junit5)
}

tasks {
    shadowJar {
        val shadedPattern = "cc.mewcraft.wakame.external."
        relocate("com.github.benmanes.caffeine.cache", shadedPattern + "caffeine")
        relocate("org.koin", shadedPattern + "koin")
        relocate("org.spongepowered.configurate", shadedPattern + "config")
        // relocate("team.unnamed.creative", "cc.mewcraft.wakame.external.resourcepack")
        // relocate("team.unnamed.hephaestus", "cc.mewcraft.wakame.external.modelengine")
        // relocate("com.github.retrooper.packetevents", "cc.mewcraft.wakame.external.packetevents.api")
        // relocate("io.github.retrooper.packetevents", "cc.mewcraft.wakame.external.packetevents.impl")

        // cloud
        // relocate("org.incendo.cloud", "cc.mewcraft.wakame.external.cloud") // We don't relocate cloud itself in this example, but you still should

        // cloud & configurate dependency
        // relocate("io.leangen.geantyref", "cc.mewcraft.wakame.external.geantyref")

        // cloud-paper dependencies
        // relocate("xyz.jpenilla.reflectionremapper", "cc.mewcraft.wakame.external.reflectionremapper")
        // relocate("net.fabricmc.mappingio", "cc.mewcraft.wakame.external.mappingio")

        // invui
        // relocate("xyz.xenondevs.invui", "cc.mewcraft.wakame.external.invui")
        // relocate("xyz.xenondevs.inventoryaccess", "cc.mewcraft.wakame.external.invui.inventoryaccess")
    }
}

sourceSets {
    main {
        blossom {
            resources {
                property("version", project.version.toString())
                property("description", project.description)
            }
        }
    }
}

buildCopy {
    fileName = "wakame-${project.version}.jar"
    archiveTask = "shadowJar"
}

dockerCopy {
    containerId = "aether-minecraft-1"
    containerPath = "/minecraft/game1/plugins/"
    fileMode = 0b110_100_100
    userId = 999
    groupId = 999
    archiveTask = "shadowJar"
}
