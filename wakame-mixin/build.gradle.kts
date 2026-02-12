plugins {
    id("koish.core-conventions")
    id("cc.mewcraft.copy-jar-docker")
    id("io.canvasmc.weaver.userdev")
    id("io.canvasmc.horizon")
}

group = "cc.mewcraft.koish"
version = "0.0.1-snapshot"
description = "The core gameplay implementation of Xiaomi's server (ignite mod)"

dependencies {
    // Horizon API
    horizon.horizonApi(local.versions.horizon.core)

    // Paper API + NMS
    paperweight.paperDevBundle(local.versions.paper)

    api(project(":wakame-api"))
    compileOnlyApi(project(":wakame-mixin-libraries"))
}

paperweight {
    // 因为:
    // - 我们不想把 paperweight 的依赖一遍又一遍的声明在每个需要的 project 里;
    // - koish-mixin 属于 “root project”, 即很多其他 project 依赖于这个 project;
    // - 为了避免 server dependency 的代码重复出现在 IJ 的代码索引里, 如查看 definition;
    // 所以将 server dependency 添加到 COMPILE_ONLY_API_CONFIGURATION.
    //addServerDependencyTo.add(project.configurations.named(JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME))
}

horizon {
    splitPluginSourceSets()
    accessTransformerFiles.from(
        file("src/main/resource-templates/widener.at")
    )
}

sourceSets {
    main {
        blossom {
            configure(project)
        }
    }
    named("plugin") {
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