plugins {
    id("wakame-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
    id("cc.mewcraft.copy-jar-build")
    id("cc.mewcraft.copy-jar-docker")
    id("io.papermc.paperweight.userdev")
    alias(local.plugins.blossom)
}

group = "cc.mewcraft.wakame"
version = "0.0.1-snapshot"
description = "The mixin part"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

dependencies {
    paperweight.paperDevBundle(local.versions.paper)

    remapper("net.fabricmc", "tiny-remapper", "0.10.4", classifier = "fat")

    compileOnly(local.ignite)
    compileOnly(local.mixin)
    compileOnly(local.mixin.extras)

    // 在本 mixin 中添加为 implementation 的依赖意味着
    // 该依赖会直接由服务端的 system classloader 加载,
    // 可以直接被服务端 (nms) 和 *任意插件* 直接访问.
    implementation(project(":wakame-api"))
    implementation(project(":wakame-common"))
    implementation(local.fleks) {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
    }
    implementation(libs.mocha)
    implementation(local.shadow.bukkit)
    implementation(local.commons.collections)
    implementation(local.commons.gson) {
        exclude("com.google.code.gson")
    }
    implementation(local.commons.provider)
    implementation(local.commons.reflection)
    implementation(local.commons.tuple)
    implementation(platform(libs.bom.configurate.yaml))
    implementation(platform(libs.bom.configurate.gson))
    implementation(platform(libs.bom.configurate.extra.kotlin))
    implementation(platform(libs.bom.configurate.extra.dfu8))
    implementation(platform(local.koin.bom))
    implementation(local.koin.core)
}

sourceSets {
    main {
        blossom {
            resources {
                property("version", project.version.toString())
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
    containerPath = "/minecraft/game1/mods/"
    fileMode = 0b110_100_100
    userId = 999
    groupId = 999
    archiveTask = "shadowJar"
}
