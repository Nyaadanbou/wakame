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

    // Paper API + NMS
    paperweight.paperDevBundle(local.versions.paper)

    // Mixin & Ignite (这些依赖由 ignite 启动器提供)
    remapper("net.fabricmc", "tiny-remapper", "0.10.4", classifier = "fat")
    compileOnly(local.ignite)
    compileOnly(local.mixin)
    compileOnly(local.mixin.extras)

    // ECS
    implementation(local.fleks) {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
    }

    // 表达式解析
    implementation(libs.mocha)

    // 反射
    implementation(local.shadow.bukkit)

    // 通用库
    implementation(local.commons.collections)
    implementation(local.commons.gson) {
        exclude("com.google.code.gson")
    }
    implementation(local.commons.gson)
    implementation(local.commons.provider)
    implementation(local.commons.reflection)
    implementation(local.commons.tuple)

    // 配置文件
    implementation(platform(libs.bom.configurate.yaml))
    implementation(platform(libs.bom.configurate.gson))
    implementation(platform(libs.bom.configurate.extra.kotlin))
    implementation(platform(libs.bom.configurate.extra.dfu8))

    // 跨进程通讯
    implementation(local.messenger)
    implementation(local.messenger.nats)
    implementation(local.messenger.rabbitmq)
    implementation(local.messenger.redis)
    implementation(local.zstdjni)
    implementation(local.jedis) {
        exclude("com.google.code.gson", "gson")
    }
    implementation(local.rabbitmq)
    implementation(local.nats)
    implementation(local.caffeine)
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
    fileName = "wakame-libraries-${project.version}.jar"
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
