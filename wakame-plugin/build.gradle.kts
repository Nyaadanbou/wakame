import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription.RelativeLoadOrder

plugins {
    id("nyaadanbou-conventions.repositories")
    id("nyaadanbou-conventions.copy-jar")
    id("wakame-conventions.kotlin")
    id("wakame-conventions.koin")
    id("io.papermc.paperweight.userdev")
    alias(libs.plugins.pluginyml.paper)
}

group = "cc.mewcraft.wakame"
version = "1.0.0-SNAPSHOT"
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
    runtimeOnly(project(":wakame-hooks:wakame-hook-vault"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-worldguard"))

    // libraries
    paperweight.paperDevBundle(local.versions.paper)
    implementation(local.commons.collections)
    implementation(local.commons.guava)
    implementation(local.commons.provider)
    implementation(local.commons.reflection)
    implementation(libs.mocha)
    compileOnly(local.shadow.nbt) // 运行时由 wakame-mixin 提供
    implementation(platform(libs.bom.adventure))
    implementation(platform(libs.bom.caffeine))
    implementation(platform(libs.bom.configurate.yaml))
    implementation(platform(libs.bom.configurate.gson))
    implementation(platform(libs.bom.configurate.kotlin))
    implementation(platform(libs.bom.creative))
    implementation(platform(libs.bom.cloud.paper))
    implementation(platform(libs.bom.cloud.kotlin)) {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
    }
    implementation(platform(libs.bom.hephaestus)) {
        exclude("com.google.code.gson")
        exclude("net.kyori")
        exclude("org.jetbrains")
    }
    implementation(platform(libs.bom.invui)) {
        exclude("org.jetbrains")
    }
    implementation(platform(libs.bom.jgit))
    implementation(platform(libs.bom.packetevents.spigot))

    // other plugins (hard dependencies)
    compileOnlyApi(local.helper)
    compileOnly(local.adventurelevel)

    // test
    testImplementation(project(":wakame-api"))
    testImplementation(local.helper)
    testImplementation(libs.configurate.yaml)
    testImplementation(libs.configurate.extra.kotlin)
    testImplementation(libs.logback.classic)
    testImplementation(libs.mockk)
    testImplementation(libs.mockbukkit)
    testImplementation(local.shadow.nbt)
}

tasks {
    shadowJar {
        // invui 的 nms 模块只能在 spigot-mapping 下运行,
        // 因此必须告知服务端我们用的是 spigot-mapping,
        // 这样才能触发 paper 的 remapping 机制.
        manifest {
            attributes["paperweight-mappings-namespace"] = "spigot"
        }

        // relocate("com.github.benmanes.caffeine.cache", "cc.mewcraft.wakame.external.caffeine")
        // relocate("org.koin", "cc.mewcraft.wakame.external.koin")
        relocate("org.spongepowered.configurate", "cc.mewcraft.wakame.external.config")
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

    // invui 依然使用 spigot-mapping; 我们必须暂时基于 spigot-mapping 构建 JAR
    assemble {
        dependsOn(reobfJar)
    }
    paperweight {
        reobfArtifactConfiguration = ReobfArtifactConfiguration.REOBF_PRODUCTION
    }
    copyJar {
        environment = "paper"
        jarTaskName = "reobfJar"
        jarFileName = "wakame-${project.version}.jar"
    }
}

paper {
    main = "cc.mewcraft.wakame.WakamePlugin"
    // loader = "cc.mewcraft.wakame.loader.WakameLoader"
    // bootstrapper = "cc.mewcraft.wakame.loader.WakameBootstrapper"
    name = "Wakame"
    version = "${project.version}"
    description = project.description
    apiVersion = "1.21"
    author = "Nailm"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    serverDependencies {
        register("helper") {
            required = true
            load = RelativeLoadOrder.BEFORE
        }
        register("AdventureLevel") {
            required = false
            load = RelativeLoadOrder.OMIT // 懒加载 class
        }
        register("ChestSort") {
            required = false
            load = RelativeLoadOrder.OMIT // 懒加载 class
        }
        register("Economy") {
            required = false
            load = RelativeLoadOrder.OMIT // 懒加载 class
        }
        register("LuckPerms") {
            required = false
            load = RelativeLoadOrder.OMIT // 懒加载 class
        }
        register("MythicMobs") {
            required = false
            load = RelativeLoadOrder.OMIT // 懒加载 class
        }
        register("Towny") {
            required = false
            load = RelativeLoadOrder.OMIT // 懒加载 class
        }
        register("Vault") {
            required = false
            load = RelativeLoadOrder.OMIT // 懒加载 class
        }
        register("WorldGuard") {
            required = false
            load = RelativeLoadOrder.OMIT // 懒加载 class
        }
    }
}