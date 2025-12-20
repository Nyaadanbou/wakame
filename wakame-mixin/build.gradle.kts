plugins {
    id("koish-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
    id("cc.mewcraft.copy-jar-build")
    id("cc.mewcraft.copy-jar-docker")
    id("io.papermc.paperweight.userdev")
    alias(local.plugins.blossom)
}

group = "cc.mewcraft.koish"
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
    // Paper API + NMS
    paperweight.paperDevBundle(local.versions.paper)

    // Mixin & Ignite (这些依赖由 Ignite 启动器提供)
    remapper("net.fabricmc", "tiny-remapper", "0.10.4", classifier = "fat")
    compileOnly(local.ignite)
    compileOnly(local.mixin)
    compileOnly(local.mixin.extras)

    // API
    api(project(":wakame-api"))

    // ECS
    compileOnlyApi(local.fleks)

    // 表达式解析
    compileOnlyApi(libs.mocha)

    // 反射
    compileOnlyApi(local.shadow.bukkit)

    // 通用库
    compileOnlyApi(local.commons.collections)
    compileOnlyApi(local.commons.gson)
    compileOnlyApi(local.commons.gson)
    compileOnlyApi(local.commons.provider)
    compileOnlyApi(local.commons.reflection)
    compileOnlyApi(local.commons.tuple)

    // 配置文件
    compileOnlyApi(platform(libs.bom.configurate.yaml))
    compileOnlyApi(platform(libs.bom.configurate.gson))
    compileOnlyApi(platform(libs.bom.configurate.extra.kotlin))
    compileOnlyApi(platform(libs.bom.configurate.extra.dfu8))

    // 跨进程通讯
    compileOnlyApi(local.messenger)
    compileOnlyApi(local.messenger.nats)
    compileOnlyApi(local.messenger.rabbitmq)
    compileOnlyApi(local.messenger.redis)
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
