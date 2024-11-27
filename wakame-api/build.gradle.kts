plugins {
    id("nyaadanbou-conventions.repositories")
    id("wakame-conventions.java")
    `maven-publish`
}

group = "cc.mewcraft.wakame"
version = "0.0.1"
description = "The API of wakame system"

dependencies {
    api(project(":wakame-common")) // 运行时由服务端提供
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
            from(components["java"])
        }
    }
}