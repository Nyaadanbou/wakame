// project description:
// 该项目是所有其他项目 (mixin, plugin, ...) 的共同依赖.

plugins {
    id("wakame-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
    id("io.papermc.paperweight.userdev")
    `maven-publish`
}

group = "cc.mewcraft.wakame"
version = "0.0.1-SNAPSHOT"

repositories {
    nyaadanbouReleases()
}

dependencies {
    paperweight.paperDevBundle(local.versions.paper)
    compileOnly(local.shadow.bukkit)
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
