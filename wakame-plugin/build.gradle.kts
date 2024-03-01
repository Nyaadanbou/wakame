import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription.RelativeLoadOrder

plugins {
    id("neko.repositories") version "1.0"
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
    compileOnly(project(":wakame-nms"))
    runtimeOnly(project(":wakame-nms", configuration = "reobf"))
    compileOnly(libs.asm) // runtime is provided by paper
    compileOnly(libs.asm.commons) // ^
    implementation(libs.bytebuddy)
    implementation(platform(libs.bom.adventure))
    implementation(platform(libs.bom.caffeine))
    implementation(platform(libs.bom.configurate.yaml))
    implementation(platform(libs.bom.configurate.kotlin))
    implementation(platform(libs.bom.creative))

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
    }

    val inputJarPath = lazy { shadowJar.get().archiveFile.get().asFile.absolutePath }
    val finalJarName = lazy { "${ext.get("name")}-${project.version}.jar" }
    val finalJarPath = lazy { layout.buildDirectory.file(finalJarName.value).get().asFile.absolutePath }
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
            exec {
                commandLine("rsync", finalJarPath.value, "dev:data/dev/jar")
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