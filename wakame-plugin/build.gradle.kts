import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription.RelativeLoadOrder

plugins {
    id("neko.repositories") version "1.0-SNAPSHOT"
    id("neko-kotlin")
    id("neko-koin")
    alias(libs.plugins.pluginyml.paper)
}

project.ext.set("name", "Wakame")

group = "cc.mewcraft.wakame"
version = "1.0.0-SNAPSHOT"
description = "Add custom stuff to server"

dependencies {
    // server
    compileOnly(libs.server.paper)

    // helper
    compileOnly("me.lucko", "helper", "6.0.0-SNAPSHOT")
    compileOnly(libs.helper.sql)
    compileOnly(libs.helper.profiles)

    // internal
    implementation(project(":wakame-api"))
    implementation(project(":wakame-common"))
    implementation(project(":wakame-ext"))
    implementation(project(":wakame-git"))
    compileOnly(project(":wakame-nms"))
    // invui 依然使用 spigot-mapping; 我们必须暂时基于 spigot-mapping 构建 JAR
    runtimeOnly(project(path = ":wakame-nms", configuration = "reobf"))

    // libraries
    compileOnly(libs.asm) // runtime is provided by paper
    compileOnly(libs.asm.commons) // ^
    implementation(libs.commons.collections)
    implementation(libs.commons.provider)
    implementation(libs.mocha)
    implementation(libs.shadow.nbt)
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

    // test
    testImplementation(libs.configurate.yaml)
    testImplementation(libs.configurate.extra.kotlin)
    testImplementation(libs.helper)
    testImplementation(libs.helper.sql)
    testImplementation(libs.helper.profiles)
    testImplementation(libs.logback.classic)
    testImplementation(libs.mockk)
    testImplementation(libs.mockbukkit)
    testImplementation(libs.server.paper)
}

tasks {
    shadowJar {
        // invui 的 nms 模块只能在 spigot-mapping 下运行,
        // 因此必须告知服务端我们用的是 spigot-mapping,
        // 这样才能触发 paper 的 remapping 机制.
        manifest {
            attributes["paperweight-mappings-namespace"] = "spigot"
        }

        relocate("com.github.benmanes.caffeine.cache", "cc.mewcraft.wakame.external.caffeine")
        relocate("org.koin", "cc.mewcraft.wakame.external.koin")
        relocate("org.spongepowered.configurate", "cc.mewcraft.wakame.external.config")
        relocate("team.unnamed.creative", "cc.mewcraft.wakame.external.resourcepack")
        relocate("team.unnamed.hephaestus", "cc.mewcraft.wakame.external.modelengine")
        relocate("com.github.retrooper.packetevents", "cc.mewcraft.wakame.external.packetevents.api")
        relocate("io.github.retrooper.packetevents", "cc.mewcraft.wakame.external.packetevents.impl")

        // cloud
        relocate("org.incendo.cloud", "cc.mewcraft.wakame.external.cloud") // We don't relocate cloud itself in this example, but you still should

        // cloud & configurate dependency
        relocate("io.leangen.geantyref", "cc.mewcraft.wakame.external.geantyref")

        // cloud-paper dependencies
        relocate("xyz.jpenilla.reflectionremapper", "cc.mewcraft.wakame.external.reflectionremapper")
        relocate("net.fabricmc.mappingio", "cc.mewcraft.wakame.external.mappingio")

        // invui
        relocate("xyz.xenondevs.invui", "cc.mewcraft.wakame.external.invui")
        relocate("xyz.xenondevs.inventoryaccess", "cc.mewcraft.wakame.external.invui.inventoryaccess")
    }

    val inputJarPath by lazy { shadowJar.get().archiveFile.get().asFile.absolutePath }
    val finalJarName by lazy { "${ext.get("name")}-${project.version}.jar" }
    val finalJarPath by lazy { layout.buildDirectory.file(finalJarName).get().asFile.absolutePath }
    val deployTargetPath = rootProject.file(".deploy_config").takeIf { it.exists() }?.readLines().orEmpty().filter { !it.startsWith('#') }
    register<Copy>("copyJar") {
        group = "mewcraft"
        dependsOn(build)
        from(inputJarPath)
        into(layout.buildDirectory)
        rename("(?i)${project.name}.*\\.jar", finalJarName)
    }
    register<Task>("deployJar") {
        group = "mewcraft"
        dependsOn(named("copyJar"))
        doLast {
            if (deployTargetPath.isEmpty()) {
                logger.lifecycle("No deploy target path found, skipping deployment")
                return@doLast
            }

            for (s in deployTargetPath) {
                logger.lifecycle("Deploying to $s...")
                exec {
                    commandLine("rsync", finalJarPath, s)
                }
            }
        }
    }
}

paper {
    main = "cc.mewcraft.wakame.WakamePlugin"
    name = project.ext.get("name") as String
    version = "${project.version}"
    description = project.description
    apiVersion = "1.19"
    author = "Nailm"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    bootstrapper = "cc.mewcraft.wakame.loader.WakameBootstrapper"
    serverDependencies {
        register("Kotlin") {
            required = true
            load = RelativeLoadOrder.BEFORE
        }
        register("helper") {
            required = true
            load = RelativeLoadOrder.BEFORE
        }
        register("MythicMobs") {
            required = false
            load = RelativeLoadOrder.OMIT
        }
    }
}