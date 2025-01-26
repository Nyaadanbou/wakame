plugins {
    id("nyaadanbou-conventions.repositories")
    id("wakame-conventions.java")
    `maven-publish`
}

group = "cc.mewcraft.wakame"
version = "0.0.1-snapshot"
description = "The API of wakame system"

dependencies {
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
            artifactId = "api"
            from(components["java"])
        }
    }
}