plugins {
    id("nyaadanbou-conventions.repositories")
    id("cc.mewcraft.build-copy")
    id("cc.mewcraft.docker-copy")
    id("wakame-conventions.kotlin")
    id("io.papermc.paperweight.userdev")
    alias(local.plugins.blossom)
}

group = "cc.mewcraft.wakame"
version = "0.0.1-snapshot"
description = "The mixin part"

dependencies {
    paperweight.paperDevBundle(local.versions.paper)

    remapper("net.fabricmc", "tiny-remapper", "0.10.4", classifier = "fat")

    compileOnly(local.ignite)
    compileOnly(local.mixin)
    compileOnly(local.mixin.extras)

    // 在本 mixin 中添加为 implementation 的依赖意味着
    // 该依赖会直接由服务端的 system classloader 加载,
    // 可以直接被服务端 (nms) 和 *任意插件* 直接访问.
    implementation(project(":wakame-api")) // 提供运行时依赖
    implementation(project(":wakame-common")) // 同上
    implementation(local.shadow.bukkit)
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
