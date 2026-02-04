pluginManagement {
    repositories {
        mavenLocal() // 为了导入 "nyaadanbou-repositories"
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("nyaadanbou-repository-settings") version "0.0.1-snapshot"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("local") {
            from(files("gradle/local.versions.toml"))
        }
    }
}

rootProject.name = "wakame"

include(":common:lazyconfig")
include(":common:messaging")
include(":standalone:bettergui-addon:koish-bridge")
include(":standalone:extra-contexts:api")
include(":standalone:extra-contexts:common")
include(":standalone:extra-contexts:paper")
include(":standalone:extra-contexts:velocity")
include(":wakame-api")
include(":wakame-hooks:wakame-hook-adventurelevel")
include(":wakame-hooks:wakame-hook-auraskills")
include(":wakame-hooks:wakame-hook-betonquest")
include(":wakame-hooks:wakame-hook-bettergui")
include(":wakame-hooks:wakame-hook-betterhud")
include(":wakame-hooks:wakame-hook-breweryx")
include(":wakame-hooks:wakame-hook-carbonchat")
include(":wakame-hooks:wakame-hook-chestshop")
// include(":wakame-hooks:wakame-hook-chestsort") // FIXME 仓库已经挂掉并且作者似乎没有修复的打算
include(":wakame-hooks:wakame-hook-craftengine")
include(":wakame-hooks:wakame-hook-economy")
include(":wakame-hooks:wakame-hook-economybridge")
include(":wakame-hooks:wakame-hook-hibiscuscommons")
include(":wakame-hooks:wakame-hook-husksync")
include(":wakame-hooks:wakame-hook-huskhomes")
include(":wakame-hooks:wakame-hook-luckperms")
include(":wakame-hooks:wakame-hook-mythicdungeons")
include(":wakame-hooks:wakame-hook-mythicmobs")
include(":wakame-hooks:wakame-hook-nightcore")
include(":wakame-hooks:wakame-hook-papi")
include(":wakame-hooks:wakame-hook-plotsquared")
include(":wakame-hooks:wakame-hook-portals")
include(":wakame-hooks:wakame-hook-quickshop")
include(":wakame-hooks:wakame-hook-thebrewingproject")
include(":wakame-hooks:wakame-hook-towny")
include(":wakame-hooks:wakame-hook-townyflight")
include(":wakame-hooks:wakame-hook-vault")
include(":wakame-hooks:wakame-hook-worldguard")
include(":wakame-mixin")
include(":wakame-mixin-libraries")
include(":wakame-plugin")
