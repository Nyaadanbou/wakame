plugins {
    id("koish-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
    `maven-publish`
}

group = "cc.mewcraft.koish"
version = "0.0.1-snapshot"
description = "The API of the core gameplay implementation"

dependencies {
    compileOnly(local.paper)
}

publishing {
    repositories {
        nyaadanbouReleases().credentials(PasswordCredentials::class)
    }
    publications {
        create<MavenPublication>("maven") {
            artifactId = "api"
            from(components["java"])
        }
    }
}