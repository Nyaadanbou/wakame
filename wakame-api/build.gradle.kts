plugins {
    id("neko-kotlin")
    id("nyaadanbou-conventions.repositories")
    `maven-publish`
}

group = "cc.mewcraft.wakame"
version = "1.0.0"
description = "The API of the core system"

dependencies {
    api(project(":wakame-common")) // 运行时由服务端提供
    compileOnly(local.paper)
    compileOnly(libs.checker.qual)
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