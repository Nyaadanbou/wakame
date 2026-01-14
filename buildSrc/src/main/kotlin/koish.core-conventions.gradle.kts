plugins {
    id("koish.kotlin-conventions")
}

configurations.all {
    // paperweight 会把 paper 自带的 configurate 加入到 compile/runtime classpath,
    // 然后一般的 exclude 对于 paperweight.paperDevBundle 没有任何效果.
    // 这导致了 paper 的 configurate 和我们自己的 fork 在 test 环境发生冲突.
    // 不过在服务端上直接跑没有这个问题 (因为 plugin classloader 有更高优先级).
    //
    // 使用以下代码直接移除所有的 spongepowered configurate 依赖.
    exclude("org.spongepowered", "configurate-core")
    exclude("org.spongepowered", "configurate-yaml")
    exclude("org.spongepowered", "configurate-gson")
}

configurations.runtimeClasspath {
    // 服务端已经自带 kotlin 的标准库和其他辅助库.
    // 排除掉 runtime 中 kotlin 和 kotlinx 依赖.
    exclude("org.jetbrains.kotlin")
    exclude("org.jetbrains.kotlinx")
}

tasks {
    shadowJar {
        val pkg = "cc.mewcraft.wakame"
        configureForPlatform(pkg, ServerPlatform.PAPER)
        relocateWithPrefix(pkg) {
            //move("com.github.luben.zstd", "zstd") // FIXME https://pastes.dev/qEtUHGgkNT
            move("cc.mewcraft.lazyconfig", "lazyconfig")
            move("cc.mewcraft.messaging2", "messaging2")
            move("com.github.benmanes.caffeine", "caffeine")
            move("com.github.quillraven.fleks", "fleks")
            move("com.googlecode.javaewah", "javaewah")
            move("com.googlecode.javaewah32", "javaewah32")
            move("com.rabbitmq", "rabbitmq")
            move("com.zaxxer.hikari", "hikari")
            move("io.leangen.geantyref", "geantyref")
            move("io.nats", "nats")
            move("javassist", "javassist")
            move("me.lucko.shadow", "shadow")
            move("net.fabricmc.mappingio", "mappingio")
            move("ninja.egg82.messenger", "messenger")
            move("org.apache.commons.codec", "apache.commons.codec")
            move("org.apache.commons.pool2", "apache.commons.pool2")
            move("org.bouncycastle", "bouncycastle")
            move("org.eclipse.jgit", "jgit")
            move("org.incendo.cloud", "cloud")
            move("org.jetbrains.exposed", "exposed")
            move("org.json", "json")
            move("org.mariadb.jdbc", "mariadb.jdbc")
            move("org.spongepowered.configurate", "configurate")
            move("redis.clients", "redis.clients")
            move("team.unnamed.creative", "creative")
            move("team.unnamed.mocha", "mocha")
            move("xyz.jpenilla.reflectionremapper", "reflectionremapper")
            move("xyz.xenondevs.commons", "xenoncommons")
        }
    }
}

repositories {
    // 在这里直接声明 repository 实际上违背了我们 Nyaadanbou 项目组的 conventions
    // 即, 所有 repositories 都应该由 cc.mewcraft.libraries-repository 这个 gradle 插件提供
    // 但为了方便, 就还是直接写在这里了, 以后也都尽量写在这里, 保持项目简洁
    configure()
}
