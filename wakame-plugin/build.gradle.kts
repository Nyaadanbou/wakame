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
    compileOnly(libs.helper.sql)
    compileOnly(libs.helper.profiles)

    // internal
    compileOnly(libs.asm) // provided by Paper runtime
    compileOnly(libs.asm.commons) // provided by Paper runtime
    implementation(project(":wakame:wakame-api"))
    implementation(project(":wakame:wakame-nms", configuration = "reobf"))
    implementation(project(":spatula:koin"))
    // implementation(project(":spatula:bukkit:message")) // TODO use adventure global translator
    implementation(project(":spatula:bukkit:utils"))
    implementation(libs.configurate.yaml) { exclude("com.google.errorprone") }
    implementation(libs.configurate.extra.kotlin)
    implementation(libs.caffeine) {
        exclude("com.google.errorprone")
        exclude("org.checkerframework")
    }
    val adventureVersion = "4.15.0"
    implementation("net.kyori", "adventure-nbt", adventureVersion)
    val creativeVersion = "1.1.0"
    implementation("team.unnamed", "creative-api", creativeVersion) {
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "adventure-key")
        exclude("org.jetbrains", "annotations")
    }
    implementation("team.unnamed", "creative-serializer-minecraft", creativeVersion) {
        exclude("team.unnamed", "creative-api")
        exclude("com.google.code.gson", "gson")
    }
    implementation("team.unnamed", "creative-server", creativeVersion) {
        exclude("team.unnamed", "creative-api")
    }

    // test
    testImplementation(libs.configurate.yaml)
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