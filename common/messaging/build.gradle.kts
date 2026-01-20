plugins {
    id("koish.kotlin-conventions")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    jmpSnapshots()
}

dependencies {
    api(project(":common:lazyconfig"))

    // Guava
    compileOnly(local.guava)

    // 冒险库 (这些平台有提供运行时, 所以 compileOnly)
    compileOnly(local.adventure.key)
    compileOnly(local.adventure.text.serializer.gson)

    // 跨进程通讯
    compileOnlyApi(local.messenger)
    compileOnlyApi(local.messenger.nats)
    compileOnlyApi(local.messenger.rabbitmq)
    compileOnlyApi(local.messenger.redis)
    compileOnlyApi(local.zstdjni)
    compileOnlyApi(local.jedis)
    compileOnlyApi(local.rabbitmq)
    compileOnlyApi(local.nats)
    compileOnlyApi(local.caffeine)
}
