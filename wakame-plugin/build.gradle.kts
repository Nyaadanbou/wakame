plugins {
    id("koish-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
    id("cc.mewcraft.copy-jar-build")
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
    // internal
    compileOnly(project(":wakame-api")) // 运行时由 koish-mod 提供
    compileOnly(project(":wakame-common")) // 运行时由 koish-mod 提供
    compileOnly(project(":wakame-mixin")) // 运行时由 koish-mod 提供
    runtimeOnly(project(":wakame-hooks:wakame-hook-adventurelevel"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-auraskills"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-betterhud"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-breweryx"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-carbonchat"))
     runtimeOnly(project(":wakame-hooks:wakame-hook-chestshop"))
    // runtimeOnly(project(":wakame-hooks:wakame-hook-chestsort")) // FIXME 仓库已经挂掉并且作者似乎没有修复的打算
     runtimeOnly(project(":wakame-hooks:wakame-hook-craftengine"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-economy"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-economybridge"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-husksync"))
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

    // libraries
    compileOnly(local.shadow.bukkit) // 运行时由 koish-mod 提供
    compileOnly(local.commons.collections)
    compileOnly(local.commons.gson)
    compileOnly(local.commons.provider)
    compileOnly(local.commons.reflection)
    compileOnly(local.commons.tuple)
    compileOnly(local.fleks) {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
    }
    compileOnly(libs.hikari)
    compileOnly(libs.mocha)
    implementation(platform(libs.bom.adventure))
    implementation(platform(libs.bom.caffeine))
    compileOnly(platform(libs.bom.exposed))
    compileOnly(platform(libs.bom.configurate.yaml)) // 运行时由 koish-mod 提供
    compileOnly(platform(libs.bom.configurate.gson))
    compileOnly(platform(libs.bom.configurate.extra.kotlin))
    compileOnly(platform(libs.bom.configurate.extra.dfu8))
    implementation(platform(libs.bom.creative))
    implementation(platform(libs.bom.cloud.paper))
    implementation(platform(libs.bom.cloud.kotlin))
    compileOnly(platform(libs.bom.invui)) /* 由自定义的 classloader 加载 */ {
        exclude("org.jetbrains")
    }
    implementation(platform(libs.bom.jgit))
    implementation(local.jdbc.mariadb)

    // other plugins (hard dependencies)
    compileOnly(local.adventurelevel)

    // test
    testImplementation(project(":wakame-api"))
    testImplementation(project(":wakame-common"))
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
    testImplementation(local.jdbc.mariadb)
    testImplementation(local.jdbc.sqlite)
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

buildCopy {
    fileName = "wakame-${project.version}.jar"
    archiveTask = "shadowJar"
}

dockerCopy {
    containerId = "aether-minecraft-1"
    containerPath = "/minecraft/game1/plugins/"
    fileMode = 0b110_100_100
    userId = 999
    groupId = 999
    archiveTask = "shadowJar"
}
