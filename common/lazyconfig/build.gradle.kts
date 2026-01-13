plugins {
    id("koish.kotlin-conventions")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

dependencies {
    // 冒险库
    compileOnly(local.adventure.api)
    compileOnly(local.adventure.extra.kotlin)
    compileOnly(local.adventure.key)
    compileOnly(local.adventure.nbt)
    compileOnly(local.adventure.text.logger.slf4j)
    compileOnly(local.adventure.text.minimessage)
    compileOnly(local.adventure.text.serializer.ansi)
    compileOnly(local.adventure.text.serializer.gson)
    compileOnly(local.adventure.text.serializer.json)
    compileOnly(local.adventure.text.serializer.legacy)
    compileOnly(local.adventure.text.serializer.plain)

    // 通用库
    api(local.commons.collections)
    api(local.commons.gson) {
        exclude("com.google.code.gson")
    }
    api(local.commons.provider)
    api(local.commons.reflection)
    api(local.commons.tuple)

    // 配置库
    api(platform(libs.bom.configurate.yaml))
    api(platform(libs.bom.configurate.gson))
    api(platform(libs.bom.configurate.extra.kotlin))
    api(platform(libs.bom.configurate.extra.dfu8))
}
