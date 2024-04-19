import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription.RelativeLoadOrder

plugins {
    id("neko.repositories") version "1.1"
    id("neko-kotlin")
    id("neko-koin")
    alias(libs.plugins.pluginyml.paper)
}

project.ext.set("name", "Wakame")

group = "cc.mewcraft.wakame"
version = "1.0.0"
description = "Add custom stuff to server"

dependencies {
    // server
    compileOnly(libs.server.purpur)

    // helper
    compileOnly(libs.helper)
    compileOnly(libs.helper.sql)
    compileOnly(libs.helper.profiles)

    // internal
    implementation(project(":wakame-api"))
    implementation(project(":wakame-common"))
    implementation(project(":wakame-ext"))
    implementation(project(":wakame-git"))
    compileOnly(project(":wakame-nms"))
    runtimeOnly(project(":wakame-nms", configuration = "reobf"))
    implementation(libs.commons.provider)
    compileOnly(libs.asm) // runtime is provided by paper
    compileOnly(libs.asm.commons) // ^
    implementation(libs.bytebuddy)
    implementation(libs.bytebuddy.agent)
    implementation(libs.mocha)
    implementation(platform(libs.bom.packetevents.spigot))
    implementation(platform(libs.bom.adventure))
    implementation(platform(libs.bom.caffeine))
    implementation(platform(libs.bom.configurate.yaml))
    implementation(platform(libs.bom.configurate.gson))
    implementation(platform(libs.bom.configurate.kotlin))
    implementation(platform(libs.bom.creative))
    implementation(platform(libs.bom.hephaestus))

    // test
    testImplementation(libs.mockk)
    testImplementation(libs.logback.classic)
    testImplementation(libs.server.purpur)
    testImplementation(libs.helper)
    testImplementation(libs.helper.sql)
    testImplementation(libs.helper.profiles)
    testImplementation(libs.configurate.yaml)
    testImplementation(libs.configurate.extra.kotlin)
}

tasks {
    shadowJar {
        relocate("com.github.benmanes.caffeine.cache", "cc.mewcraft.wakame.external.caffeine")
        relocate("io.leangen.geantyref", "cc.mewcraft.wakame.external.geantyref")
        relocate("org.koin", "cc.mewcraft.wakame.external.koin")
        relocate("org.spongepowered.configurate", "cc.mewcraft.wakame.external.config")
        relocate("team.unnamed.creative", "cc.mewcraft.wakame.external.resourcepack")
        relocate("team.unnamed.hephaestus", "cc.mewcraft.wakame.external.modelengine")
        relocate("com.github.retrooper.packetevents", "cc.mewcraft.wakame.external.packetevents.api")
        relocate("io.github.retrooper.packetevents", "cc.mewcraft.wakame.external.packetevents.impl")
        // relocate("net.kyori", "cc.mewcraft.wakame.external.packetevents.kyori")
    }

    val inputJarPath = lazy { shadowJar.get().archiveFile.get().asFile.absolutePath }
    val finalJarName = lazy { "${ext.get("name")}-${project.version}.jar" }
    val finalJarPath = lazy { layout.buildDirectory.file(finalJarName.value).get().asFile.absolutePath }
    val deployTargetPath = rootProject.file(".deploy_config").takeIf { it.exists() }?.readText()?.takeIf { it.isNotBlank() }
    register<Copy>("copyJar") {
        group = "mewcraft"
        dependsOn(build)
        from(inputJarPath.value)
        into(layout.buildDirectory)
        rename("(?i)${project.name}.*\\.jar", finalJarName.value)
    }
    register<Task>("deployJar") {
        group = "mewcraft"
        dependsOn(named("copyJar"))
        doLast {
            if (deployTargetPath == null) {
                logger.lifecycle("No deploy target path found, skipping deployment")
                return@doLast
            }

            exec {
                commandLine("rsync", finalJarPath.value, deployTargetPath)
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