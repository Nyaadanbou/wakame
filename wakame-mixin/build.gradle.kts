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
description = "The core gameplay implementation of Xiaomi's server (ignite mod)"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

paperweight {
    // 因为:
    // - 我们不想把 paperweight 的依赖一遍又一遍的声明在每个需要的 project 里;
    // - koish-mixin 属于 “root project”, 即很多其他 project 依赖于这个 project;
    // - 为了避免 server dependency 的代码重复出现在 IJ 的代码索引里, 如查看 definition;
    // 所以将 server dependency 添加到 COMPILE_ONLY_API_CONFIGURATION.
    addServerDependencyTo.add(project.configurations.named(JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME))
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
    implementation(libs.hikari) {
        exclude("org.slf4j", "slf4j-api")
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
    implementation(platform(libs.bom.exposed))
    implementation(platform(libs.bom.configurate.yaml))
    implementation(platform(libs.bom.configurate.gson))
    implementation(platform(libs.bom.configurate.extra.kotlin))
    implementation(platform(libs.bom.configurate.extra.dfu8))
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
