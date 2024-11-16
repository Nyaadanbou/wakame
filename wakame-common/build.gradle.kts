// project description:
// 该项目是所有其他项目 (mixin, plugin, ...) 的共同依赖.

plugins {
    id("neko-kotlin")
    id("nyaadanbou-conventions.repositories")
    `maven-publish`
}

version = "0.0.1"

dependencies {
    compileOnly(local.paper)
    compileOnly(local.shadow.nbt)
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
            from(components["java"])
        }
    }
}
