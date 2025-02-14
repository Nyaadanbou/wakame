@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mewcraft.cc/releases")
        maven("https://repo.mewcraft.cc/private") {
            credentials {
                username = providers.gradleProperty("nyaadanbou.mavenUsername").orNull
                password = providers.gradleProperty("nyaadanbou.mavenPassword").orNull
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        maven("https://repo.mewcraft.cc/releases")
        maven("https://repo.mewcraft.cc/private") {
            credentials {
                username = providers.gradleProperty("nyaadanbou.mavenUsername").orNull
                password = providers.gradleProperty("nyaadanbou.mavenPassword").orNull
            }
        }
    }
    versionCatalogs {
        create("local") {
            from(files("gradle/local.versions.toml"))
        }
    }
    versionCatalogs {
        create("libs") {
            from("cc.mewcraft.gradle:catalog:0.5")
        }
    }
}

rootProject.name = "wakame"

include(":wakame-api")
include(":wakame-common")
include(":koish-features:koish-feature-enchantment-flan")
include(":koish-features:koish-feature-enchantment-nyaa")
include(":wakame-hooks:wakame-hook-adventurelevel")
include(":wakame-hooks:wakame-hook-chestsort")
include(":wakame-hooks:wakame-hook-economy")
include(":wakame-hooks:wakame-hook-luckperms")
include(":wakame-hooks:wakame-hook-mythicmobs")
include(":wakame-hooks:wakame-hook-towny")
include(":wakame-hooks:wakame-hook-townyflight")
include(":wakame-hooks:wakame-hook-vault")
include(":wakame-hooks:wakame-hook-worldguard")
include(":wakame-mixin")
include(":wakame-plugin")
