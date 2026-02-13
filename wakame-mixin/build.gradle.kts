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