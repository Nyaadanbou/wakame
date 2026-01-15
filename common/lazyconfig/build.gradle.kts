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
    //compileOnlyApi(local.adventure.api)
    //compileOnlyApi(local.adventure.extra.kotlin)
    compileOnlyApi(local.adventure.key)
    //compileOnlyApi(local.adventure.nbt)
    compileOnlyApi(local.adventure.text.logger.slf4j)
    //compileOnlyApi(local.adventure.text.minimessage)
    //compileOnlyApi(local.adventure.text.serializer.ansi)
    //compileOnlyApi(local.adventure.text.serializer.gson)
    //compileOnlyApi(local.adventure.text.serializer.json)
    //compileOnlyApi(local.adventure.text.serializer.legacy)
    //compileOnlyApi(local.adventure.text.serializer.plain)

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
