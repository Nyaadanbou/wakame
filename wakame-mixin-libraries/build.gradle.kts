plugins {
    id("koish.core-conventions")
    id("cc.mewcraft.libraries-repository")
    id("cc.mewcraft.copy-jar-docker")
    alias(local.plugins.blossom)
}

group = "cc.mewcraft.koish"
version = "0.0.1-snapshot"
description = "This JAR contains the dependencies for other subprojects."

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

dependencies {
    // 写在最前面:
    // 在本 mixin project 中添加为 implementation 的依赖意味着
    // 该依赖会直接由服务端的 system classloader 加载,
    // 可以直接被服务端 (nms) 和 *任意插件* 直接访问.

    // ASM (这些依赖运行时由 paper 服务端提供)
    compileOnly(local.asm)

    // Mixin (这些依赖运行时由 ignite 启动器提供)
    compileOnly(local.mixin)
    compileOnly(local.mixin.extras)

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
    api(local.commons.provider)
    api(local.commons.reflection)
    api(local.commons.tuple)

    // 配置
    api(project(":common:lazyconfig"))

    // 跨进程通讯
    api(local.messenger)
    api(local.messenger.nats)
    api(local.messenger.rabbitmq)
    api(local.messenger.redis)
    api(local.zstdjni)
    api(local.jedis) {
        exclude("com.google.code.gson", "gson")
    }
    api(local.rabbitmq)
    api(local.nats)
    api(local.caffeine)
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

// 故意不写 builderCopy 因为会导致 gradle 无法执行 copyJarToBuild

dockerCopy {
    containerId = "aether-minecraft-1"
    containerPath = "/minecraft/game1/mods/"
    fileMode = 0b110_100_100
    userId = 999
    groupId = 999
    archiveTask = "shadowJar"
}
