// project description:
// 该项目是所有其他项目 (mixin, plugin, ...) 的共同依赖.

plugins {
    id("wakame-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
    `maven-publish`
}

group = "cc.mewcraft.wakame"
version = "0.0.1-SNAPSHOT"
description = "The common code of the core gameplay implementation"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

dependencies {
    // 我们希望 koish-common 的代码趋于通用/稳定/低依赖, 因此不包含 NMS
    compileOnly(local.paper)
}

publishing {
    repositories {
        maven("https://repo.mewcraft.cc/private") {
            credentials {
                username = providers.gradleProperty("nyaadanbou.mavenUsername").orNull
                password = providers.gradleProperty("nyaadanbou.mavenPassword").orNull
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            artifactId = "common"
            from(components["java"])
        }
    }
}
