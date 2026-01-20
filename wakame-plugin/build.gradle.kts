plugins {
    id("koish.core-conventions")
    id("cc.mewcraft.copy-jar-docker")
}

group = "cc.mewcraft.koish"
version = "0.0.1-snapshot"
description = "The core gameplay implementation of Xiaomi's server (paper plugin)"

dependencies {
    /* internal */

    // api + mixin: 运行时由 mod 提供
    compileOnlyApi(project(":wakame-api"))
    compileOnlyApi(project(":wakame-mixin"))

    // Hooks
    runtimeOnly(project(":wakame-hooks:wakame-hook-adventurelevel"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-auraskills"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-betonquest"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-bettergui"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-betterhud"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-breweryx"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-carbonchat"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-chestshop"))
    // runtimeOnly(project(":wakame-hooks:wakame-hook-chestsort")) // FIXME 仓库已经挂掉并且作者似乎没有修复的打算
    runtimeOnly(project(":wakame-hooks:wakame-hook-craftengine"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-economy"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-economybridge"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-husksync"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-huskhomes"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-luckperms"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-mythicdungeons"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-mythicmobs"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-papi"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-quickshop"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-thebrewingproject"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-towny"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-townyflight"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-vault"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-worldguard"))

    /* libraries */

    // Gremlin
    implementation(local.gremlin.runtime)
    implementation(local.jarrelocator)

    // 通用
    //koishLoader(local.commons.collections)
    //koishLoader(local.commons.gson)
    //koishLoader(local.commons.provider)
    //koishLoader(local.commons.reflection)
    //koishLoader(local.commons.tuple)

    // 配置
    //koishLoader(local.configurate.yaml)
    //koishLoader(local.configurate.gson)
    //koishLoader(local.configurate.extra.dfu8)
    //koishLoader(local.configurate.extra.kotlin)

    // 数据库
    koishLoader(local.exposed.core)
    koishLoader(local.exposed.dao)
    koishLoader(local.exposed.jdbc)
    koishLoader(local.h2)
    koishLoader(local.mariadb.jdbc)
    koishLoader(local.mysql.jdbc)
    koishLoader(local.postgresql.jdbc)
    koishLoader(local.hikaricp)

    // 跨进程通讯
    //koishLoader(local.messenger)
    //koishLoader(local.messenger.nats)
    //koishLoader(local.messenger.rabbitmq)
    //koishLoader(local.messenger.redis)
    //koishLoader(local.zstdjni)
    //koishLoader(local.jedis)
    //koishLoader(local.rabbitmq)
    //koishLoader(local.nats)
    //koishLoader(local.caffeine)

    // 指令框架
    koishLoader(local.cloud.core)
    koishLoader(local.cloud.paper)
    koishLoader(local.cloud.minecraft.extras)
    koishLoader(local.cloud.kotlin.extensions)
    koishLoader(local.cloud.kotlin.coroutines)

    // 原版UI
    koishLoader(local.adventure.nbt)
    koishLoader(local.adventure.extra.kotlin)

    // 箱子UI
    koishLoader(local.invui.core)
    koishLoader(local.invui.inventoryaccess)
    koishLoader(local.invui.inventoryaccess.r24)
    koishLoader(local.invui.inventoryaccess.r25)
    koishLoader(local.invui.inventoryaccess.r26)
    koishLoader(local.invui.kotlin)

    // 资源包
    koishLoader(local.creative.api)
    koishLoader(local.creative.serializer.minecraft)
    koishLoader(local.creative.server)

    // Git
    koishLoader(local.jgit)

    /* test environment (just add whatever it needs to compile) */

    testImplementation(project(":wakame-mixin"))
    testImplementation(local.mockk)
    testImplementation(local.logback.classic)
}

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

configurations {
    runtimeDownload {
        excludePlatformRuntime(ServerPlatform.PAPER)
    }
}
