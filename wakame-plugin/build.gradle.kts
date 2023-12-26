import net.minecrell.pluginyml.paper.PaperPluginDescription.RelativeLoadOrder

plugins {
    id("cc.mewcraft.repo-conventions")
    id("cc.mewcraft.kotlin-conventions")
    id("cc.mewcraft.deploy-conventions")
    alias(libs.plugins.pluginyml.paper)
}

project.ext.set("name", "Wakame")

group = "cc.mewcraft.wakame"
version = "1.0.0"
description = "Add custom stuff to server"

repositories {
    maven("https://repo.unnamed.team/repository/unnamed-public/")
}

dependencies {
    // server
    compileOnly(libs.server.paper)

    // helper
    compileOnly(libs.helper)

    // internal
    implementation(project(":wakame:wakame-api"))
    implementation(project(":wakame:wakame-nms", configuration = "reobf"))
    implementation(project(":spatula:koin"))
    implementation(project(":spatula:bukkit:message"))
    implementation(libs.configurate)
    compileOnly(libs.bundles.mccoroutine.bukkit) // already shaded by helper JAR
    implementation("net.kyori", "adventure-nbt", "4.14.0")
    implementation("team.unnamed", "creative-api", "1.1.0")
    implementation("team.unnamed", "creative-serializer-minecraft", "1.1.0")
    implementation("team.unnamed", "creative-server", "1.1.0")
}

paper {
    main = "cc.mewcraft.wakame.WakamePlugin"
    name = project.ext.get("name") as String
    version = "${project.version}"
    description = project.description
    apiVersion = "1.19"
    author = "Nailm"
    serverDependencies {
        register("Kotlin") {
            required = true
            load = RelativeLoadOrder.BEFORE
        }
        register("helper") {
            required = true
            load = RelativeLoadOrder.BEFORE
        }
    }
}