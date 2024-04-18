@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositories {
        maven("https://repo.mewcraft.cc/releases")
        maven("https://repo.mewcraft.cc/private") {
            credentials {
                username = providers.gradleProperty("mewcraftRepositoryUsername").getOrElse("")
                password = providers.gradleProperty("mewcraftRepositoryPassword").getOrElse("")
            }
        }
    }
    versionCatalogs {
        create("libs") {
            from("cc.mewcraft.gradle:catalog:1.0")
        }
    }
}
