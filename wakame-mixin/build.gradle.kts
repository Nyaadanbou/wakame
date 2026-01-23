plugins {
    id("koish.core-conventions")
    id("cc.mewcraft.copy-jar-docker")
    id("io.papermc.paperweight.userdev")
}

group = "cc.mewcraft.koish"
version = "0.0.1-snapshot"
description = "The core gameplay implementation of Xiaomi's server (ignite mod)"

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
    remapper("net.fabricmc:tiny-remapper:0.10.4:fat")
    compileOnly(local.ignite)
    compileOnly(local.mixin)
    compileOnly(local.mixin.extras)

    api(project(":wakame-api"))
    compileOnlyApi(project(":wakame-mixin-libraries"))
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
    containerPath = "/minecraft/game1/mods/"
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