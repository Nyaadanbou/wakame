plugins {
    id("wakame-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
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
        nyaadanbouPrivate()
    }
    publications {
        create<MavenPublication>("maven") {
            artifactId = "api"
            from(components["java"])
        }
    }
}