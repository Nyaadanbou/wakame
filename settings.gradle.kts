@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(uri("${System.getProperty("user.home")}/MewcraftRepository"))
    }
}

dependencyResolutionManagement {
    repositories {
        maven(uri("${System.getProperty("user.home")}/MewcraftRepository"))
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
include(":wakame-nms")
include(":wakame-plugin")
