plugins {
    id("nyaadanbou-conventions.repositories")
    id("nyaadanbou-conventions.copy-jar")
    id("wakame-conventions.kotlin")
    id("io.papermc.paperweight.userdev")
}

group = "cc.mewcraft.wakame"
version = "0.0.1-snapshot"
description = "The mixin part"

dependencies {
    paperweight.paperDevBundle(local.versions.paper)

    remapper("net.fabricmc", "tiny-remapper", "0.10.1", classifier = "fat")

    compileOnly(local.ignite)
    compileOnly(local.mixin)
    compileOnly(local.mixin.extras)

    // 在本 mixin 中添加为 implementation 的依赖意味着
    // 该依赖会直接由服务端的 system classloader 加载,
    // 可以直接被服务端 (nms) 和 *任意插件* 直接访问.
    implementation(project(":wakame-api")) // 提供运行时依赖
    implementation(project(":wakame-common")) // 同上
    implementation(local.shadow.nbt)
}

tasks {
    processResources {
        expand(project.properties)
    }
    copyJar {
        environment = "paperMixin"
        jarFileName = "wakame-${project.version}.jar"
    }
}

paperweight {
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}
