plugins {
    id("wakame-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
    id("cc.mewcraft.copy-jar-build")
    id("cc.mewcraft.copy-jar-docker")
    id("io.papermc.paperweight.userdev")
    alias(local.plugins.blossom)
}

group = "cc.mewcraft.wakame"
version = "0.0.1-snapshot"
description = "The core plugin of Nyaadanbou"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

dependencies {
    // internal
    compileOnlyApi(project(":wakame-api")) // 运行时由 wakame-mixin 提供
    compileOnlyApi(project(":wakame-common")) // 同上
    compileOnlyApi(project(":wakame-mixin")) // 同上
    compileOnly(project(":wakame-mixin"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-adventurelevel"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-chestsort"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-economy"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-luckperms"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-mythicmobs"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-towny"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-townyflight"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-vault"))
    runtimeOnly(project(":wakame-hooks:wakame-hook-worldguard"))

    // libraries
    paperweight.paperDevBundle(local.versions.paper)
    compileOnly(platform(local.koin.bom)) // 运行时由 koish-mod 提供
    compileOnly(local.koin.core)
    compileOnly(local.shadow.bukkit) // 运行时由 koish-mod 提供
    compileOnly(local.commons.collections)
    compileOnly(local.commons.gson)
    compileOnly(local.commons.provider)
    compileOnly(local.commons.reflection)
    compileOnly(local.commons.tuple)
    implementation(local.fleks) {
        exclude("org.jetbrains")
    }
    implementation(libs.mocha)
    implementation(local.snakeyaml.engine)
    implementation(platform(libs.bom.adventure))
    implementation(platform(libs.bom.caffeine))
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

    // other plugins (hard dependencies)
    compileOnly(local.adventurelevel)

    // test
    testImplementation(project(":wakame-api"))
    testImplementation(project(":wakame-common"))
    testImplementation(project(":wakame-mixin"))
    testImplementation(libs.mockk)
    testImplementation(libs.logback.classic)
    testImplementation(platform(local.koin.bom)) // koin 的 junit5 模块要求这个必须出现在 testRuntime
    testImplementation(local.koin.core)
    testImplementation(local.koin.test.junit5)
    testImplementation(local.shadow.bukkit)
    testImplementation(local.commons.collections)
    testImplementation(local.commons.gson)
    testImplementation(local.commons.provider)
    testImplementation(local.commons.reflection)
    testImplementation(local.commons.tuple)
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
