@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositories {
        maven("https://repo.mewcraft.cc/releases")
        maven("https://repo.mewcraft.cc/private") {
            credentials {
                username = providers.gradleProperty("nyaadanbouUsername").getOrElse("")
                password = providers.gradleProperty("nyaadanbouPassword").getOrElse("")
            }
        }
    }
    versionCatalogs {
        create("local") {
            from(files("../gradle/local.versions.toml"))
        }
    }
    versionCatalogs {
        create("libs") {
            from("cc.mewcraft.gradle:catalog:1.0-SNAPSHOT")
        }
    }
}
