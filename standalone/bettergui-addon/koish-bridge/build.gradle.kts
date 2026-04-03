plugins {
    id("koish.bettergui-addon-conventions")
    id("cc.mewcraft.copy-jar-docker")
}

version = "0.0.1"

repositories {
    jmpSnapshots()
}

dependencies {
    compileOnly(project(":wakame-plugin"))
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
    containerPath = "/minecraft/game1/plugins/BetterGUI/addon"
    fileMode = 0b110_100_100
    userId = 999
    groupId = 999
    archiveTask = "shadowJar"
}

tasks {
    shadowJar {
        archiveBaseName.set("KoishBridge")
    }
}