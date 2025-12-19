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
    // 写在最前面:
    // 在本 mixin project 中添加为 implementation 的依赖意味着
    // 该依赖会直接由服务端的 system classloader 加载,
    // 可以直接被服务端 (nms) 和 *任意插件* 直接访问.

    // Paper API + NMS
    paperweight.paperDevBundle(local.versions.paper)

    // Mixin & Ignite (这些依赖由 ignite 启动器提供)
    remapper("net.fabricmc", "tiny-remapper", "0.10.4", classifier = "fat")
    compileOnly(local.ignite)
    compileOnly(local.mixin)
    compileOnly(local.mixin.extras)

    // 内部模块
    api(project(":wakame-api"))
    api(project(":wakame-common"))

    // ECS
    api(local.fleks) {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
    }

    // 表达式解析
    api(libs.mocha)

    // 反射
    api(local.shadow.bukkit)

    // 通用库
    api(local.commons.collections)
    api(local.commons.gson) {
        exclude("com.google.code.gson")
    }
    api(local.commons.gson)
    api(local.commons.provider)
    api(local.commons.reflection)
    api(local.commons.tuple)

    // 配置文件
    api(platform(libs.bom.configurate.yaml))
    api(platform(libs.bom.configurate.gson))
    api(platform(libs.bom.configurate.extra.kotlin))
    api(platform(libs.bom.configurate.extra.dfu8))

    // 跨进程通讯
    api(local.messenger)
    implementation(local.messenger.nats)
    implementation(local.messenger.rabbitmq)
    implementation(local.messenger.redis)
    implementation(local.zstdjni)
    implementation(local.jedis) {
        exclude("com.google.code.gson", "gson")
    }
    implementation(local.rabbitmq)
    implementation(local.nats)
    runtimeOnly(local.caffeine)
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
