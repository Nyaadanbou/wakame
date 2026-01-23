plugins {
    id("koish.core-conventions")
    id("cc.mewcraft.copy-jar-docker")
    //id("io.canvasmc.weaver.userdev")
    //id("io.canvasmc.horizon")
}

group = "cc.mewcraft.koish"
version = "0.0.1-snapshot"
description = "This JAR contains the dependencies for other subprojects."

repositories {
    mavenLocal {
        content {
            includeGroup("io.canvasmc.horizon")
        }
    }
}

dependencies {
    // 写在最前面:
    // 在本 mixin project 中添加为 implementation 的依赖意味着
    // 该依赖会直接由服务端的 system classloader 加载,
    // 可以直接被服务端 (nms) 和 *任意插件* 直接访问.

    // Horizon API
    //horizon.horizonApi(local.versions.horizon.core)

    // Paper API + NMS
    //paperweight.paperDevBundle(local.versions.paper)

    // ECS
    api(local.fleks)

    // 表达式解析
    api(local.mocha)

    // 反射
    api(local.shadow.bukkit)

    // 配置
    api(project(":common:lazyconfig"))
    runtimeOnly(local.commons.collections)
    runtimeOnly(local.commons.gson)
    runtimeOnly(local.commons.provider)
    runtimeOnly(local.commons.reflection)
    runtimeOnly(local.commons.tuple)
    runtimeOnly(local.configurate.yaml)
    runtimeOnly(local.configurate.gson)
    runtimeOnly(local.configurate.extra.dfu8)
    runtimeOnly(local.configurate.extra.kotlin)

    // 跨进程通讯
    api(project(":common:messaging"))
    runtimeOnly(local.messenger)
    runtimeOnly(local.messenger.nats)
    runtimeOnly(local.messenger.rabbitmq)
    runtimeOnly(local.messenger.redis)
    runtimeOnly(local.zstdjni)
    runtimeOnly(local.jedis)
    runtimeOnly(local.rabbitmq)
    runtimeOnly(local.nats)
    runtimeOnly(local.caffeine)
}

//horizon {
//
//}

sourceSets {
    main {
        blossom {
            configure(project)
        }
    }
}

dockerCopy {
    containerId = "aether-minecraft-1"
    containerPath = "/minecraft/game1/plugins/"
    fileMode = 0b110_100_100
    userId = 999
    groupId = 999
    archiveTask = "shadowJar"
}

tasks {
    shadowJar {
        configure(ServerPlatform.PAPER)
    }
}