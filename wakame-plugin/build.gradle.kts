plugins {
    id("koish.core-conventions")
    id("cc.mewcraft.libraries-repository")
    id("cc.mewcraft.copy-jar-docker")
    alias(local.plugins.blossom)
}

group = "cc.mewcraft.koish"
version = "0.0.1-snapshot"
description = "The core gameplay implementation of Xiaomi's server (paper plugin)"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

dependencies {
    /* internal */

    //region 运行时由 koish-mod 提供
    compileOnlyApi(project(":wakame-mixin"))
    //endregion
    runtimeOnly(project(":wakame-hooks:wakame-hook-adventurelevel"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-auraskills"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-betonquest"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-bettergui"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-betterhud"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-breweryx"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-carbonchat"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-chestshop"))
    // FIXME 仓库已经挂掉并且作者似乎没有修复的打算
    // runtimeOnly(project(":wakame-hooks:wakame-hook-chestsort"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-craftengine"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-economy"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-economybridge"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-husksync"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-huskhomes"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-luckperms"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-mythicdungeons"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-mythicmobs"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-papi"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-quickshop"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-thebrewingproject"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-towny"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-townyflight"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-vault"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-worldguard"))

    /* libraries */

    // 数据库
    api(platform(libs.bom.exposed))
    implementation(local.hikaricp) {
        exclude("org.slf4j", "slf4j-api")
    }
    implementation(local.mariadb.jdbc) {
        exclude("org.slf4j", "slf4j-api")
    }

    // 原版UI
    api(platform(libs.bom.adventure))

    // 箱子UI (该依赖将由自定义的 classloader 加载, 所以这里是 compileOnly)
    compileOnly(platform(libs.bom.invui)) {
        exclude("org.jetbrains")
    }

    // 资源包
    api(platform(libs.bom.creative))

    // 指令框架
    implementation(platform(libs.bom.cloud.paper))
    implementation(platform(libs.bom.cloud.kotlin))

    // Git
    implementation(platform(libs.bom.jgit))

    /* test environment (just add whatever we need) */

    testImplementation(project(":wakame-api"))
    testImplementation(project(":wakame-mixin"))
    testImplementation(libs.mockk) {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
    }
    testImplementation(libs.logback.classic)
    testImplementation(local.shadow.bukkit)
    testImplementation(local.commons.collections)
    testImplementation(local.commons.gson)
    testImplementation(local.commons.provider)
    testImplementation(local.commons.reflection)
    testImplementation(local.commons.tuple)
    testImplementation(local.paper)
    testImplementation(local.datafixerupper)
    testImplementation(local.kotlinx.serialization.core)
    testImplementation(local.mariadb.jdbc)
    testImplementation(local.sqlite.jdbc)
    testImplementation(platform(libs.bom.exposed))
    testImplementation(platform(libs.bom.configurate.yaml))
    testImplementation(platform(libs.bom.configurate.gson))
    testImplementation(platform(libs.bom.configurate.extra.kotlin))
    testImplementation(platform(libs.bom.configurate.extra.dfu8))
}

sourceSets {
    main {
        blossom {
            resources {
                property("version", project.version.toString())
                property("description", project.description)
            }
        }
    }
}

dockerCopy {
    containerId = "aether-minecraft-1"
    containerPath = "/minecraft/game1/plugins/"
    fileMode = 0b110_100_100
    userId = 999
    groupId = 999
    archiveTask = "shadowJar"
}
