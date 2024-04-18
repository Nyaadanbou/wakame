@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mewcraft.cc/releases")
        maven("https://repo.mewcraft.cc/private") {
            credentials {
                username = providers.gradleProperty("mewcraftRepositoryUsername").getOrElse("")
                password = providers.gradleProperty("mewcraftRepositoryPassword").getOrElse("")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

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

rootProject.name = "wakame"

include(":wakame-api")
include(":wakame-common")
include(":wakame-ext")
include(":wakame-git")
include(":wakame-nms")
include(":wakame-plugin")
