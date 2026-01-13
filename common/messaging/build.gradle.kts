plugins {
    id("koish.kotlin-conventions")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    configure()
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

dependencies {
    api(project(":common:lazyconfig"))

    // Guava
    compileOnly(local.guava)

    // 冒险库
    compileOnly(local.adventure.api)
    compileOnly(local.adventure.key)
    compileOnly(local.adventure.text.serializer.gson)

    // 跨进程通讯
    api(local.messenger)
    api(local.messenger.nats)
    api(local.messenger.rabbitmq)
    api(local.messenger.redis)
    api(local.zstdjni)
    api(local.jedis) {
        exclude("com.google.code.gson", "gson")
    }
    api(local.rabbitmq)
    api(local.nats)
    api(local.caffeine)
}
