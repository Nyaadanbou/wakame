import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription.RelativeLoadOrder

plugins {
    id("nyaadanbou-conventions.repositories")
    id("nyaadanbou-conventions.copy-jar")
    id("wakame-conventions.kotlin")
    id("io.papermc.paperweight.userdev")
    alias(libs.plugins.pluginyml.paper)
}

group = "cc.mewcraft.wakame"
version = "0.0.1-snapshot"
description = "The core plugin of Nyaadanbou"

dependencies {
    // internal
    compileOnlyApi(project(":wakame-api")) // 运行时由 wakame-mixin 提供
    compileOnlyApi(project(":wakame-common")) // 同上
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
    compileOnlyApi(local.shadow.nbt) // 运行时由 wakame-mixin 提供
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
    implementation(platform(libs.bom.hephaestus)) {
        exclude("com.google.code.gson")
        exclude("net.kyori")
        exclude("org.jetbrains")
    }
    compileOnly(platform(libs.bom.invui)) { // 由自定义的 classloader 加载
        exclude("org.jetbrains")
    }
    implementation(platform(libs.bom.jgit))
    implementation(platform(libs.bom.packetevents.spigot))

    // other plugins (hard dependencies)
    compileOnly(local.adventurelevel)

    // test
    testImplementation(project(":wakame-api"))
    testImplementation(project(":wakame-common"))
    testImplementation(libs.logback.classic)
    testImplementation(libs.mockk)
    testImplementation(local.shadow.nbt)
    testImplementation(local.koin.test.junit5)
}

tasks {
    shadowJar {
        // 2025/2/2 更新: 使用自定义的 classloader 加载 InvUI 依赖
        //
        // invui 的 nms 模块只能在 spigot-mapping 下运行,
        // 因此必须告知服务端我们用的是 spigot-mapping,
        // 这样才能触发 paper 的 remapping 机制.
        //manifest {
        //    attributes["paperweight-mappings-namespace"] = "spigot"
        //}

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

    // 2025/2/2 更新: 使用自定义的 classloader 加载 InvUI 依赖
    //
    // invui 依然使用 spigot-mapping; 我们必须暂时基于 spigot-mapping 构建 JAR
    //assemble {
    //    dependsOn(reobfJar)
    //}

    // 2025/2/2 更新: 使用自定义的 classloader 加载 InvUI 依赖
    //
    //paperweight {
    //    reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION
    //}

    copyJar {
        environment = "paper"
        // 2025/2/2 更新: 使用自定义的 classloader 加载 InvUI 依赖
        //jarTaskName = "reobfJar"
        jarFileName = "wakame-${project.version}.jar"
    }
}

paper {
    main = "cc.mewcraft.wakame.Koish"
    loader = "cc.mewcraft.wakame.KoishLoader"
    bootstrapper = "cc.mewcraft.wakame.KoishBootstrapper"
    name = "Wakame"
    version = "${project.version}"
    description = project.description
    apiVersion = "1.21"
    author = "Nailm"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    serverDependencies {
        register("AdventureLevel") {
            required = false
            load = RelativeLoadOrder.BEFORE
        }
        register("ChestSort") {
            required = false
            load = RelativeLoadOrder.OMIT
        }
        register("Economy") {
            required = false
            load = RelativeLoadOrder.OMIT
        }
        register("LuckPerms") {
            required = false
            load = RelativeLoadOrder.OMIT
        }
        register("MythicMobs") {
            required = false
            load = RelativeLoadOrder.OMIT
        }
        register("Towny") {
            required = false
            load = RelativeLoadOrder.OMIT
        }
        register("TownyFlight") {
            required = false
            load = RelativeLoadOrder.OMIT
        }
        register("Vault") {
            required = false
            load = RelativeLoadOrder.OMIT
        }
        register("WorldGuard") {
            required = false
            load = RelativeLoadOrder.OMIT
        }
    }
}