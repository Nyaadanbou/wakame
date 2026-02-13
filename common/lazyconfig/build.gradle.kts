plugins {
    id("koish.kotlin-conventions")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
}

dependencies {
    // 冒险库
    compileOnly(local.adventure.key)
    compileOnly(local.adventure.text.logger.slf4j)

    // 通用库
    compileOnlyApi(local.commons.collections)
    compileOnlyApi(local.commons.gson)
    compileOnlyApi(local.commons.provider)
    compileOnlyApi(local.commons.reflection)
    compileOnlyApi(local.commons.tuple)

    // 配置库
    compileOnlyApi(local.configurate.yaml)
    compileOnlyApi(local.configurate.gson)
    compileOnlyApi(local.configurate.extra.dfu9)
    compileOnlyApi(local.configurate.extra.kotlin)
}
