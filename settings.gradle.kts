pluginManagement {
    repositories {
        mavenLocal() // 为了导入 "nyaadanbou-repositories"
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("nyaadanbou-repository") version "0.0.1-snapshot"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("local") {
            from(files("gradle/local.versions.toml"))
        }
    }
    versionCatalogs {
        create("libs") {
            from("cc.mewcraft.gradle:catalog:0.11-SNAPSHOT")
        }
    }
}

rootProject.name = "wakame"

include(":wakame-api")
include(":wakame-common")
include(":wakame-hooks:wakame-hook-adventurelevel")
include(":wakame-hooks:wakame-hook-betterhud")
include(":wakame-hooks:wakame-hook-breweryx")
include(":wakame-hooks:wakame-hook-chestsort")
include(":wakame-hooks:wakame-hook-economy")
include(":wakame-hooks:wakame-hook-economybridge")
include(":wakame-hooks:wakame-hook-luckperms")
include(":wakame-hooks:wakame-hook-mythicmobs")
include(":wakame-hooks:wakame-hook-papi")
include(":wakame-hooks:wakame-hook-quickshop")
include(":wakame-hooks:wakame-hook-towny")
include(":wakame-hooks:wakame-hook-townyflight")
include(":wakame-hooks:wakame-hook-vault")
include(":wakame-hooks:wakame-hook-worldguard")
include(":wakame-mixin")
include(":wakame-plugin")
