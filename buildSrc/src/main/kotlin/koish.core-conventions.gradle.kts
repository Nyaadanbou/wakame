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
        archiveClassifier.set("shaded")

        mergeServiceFiles()

        dependencies {

            // 没啥用的元数据文件
            exclude("about.html")
            exclude("META-INF/maven/**")
            exclude("META-INF/licenses/**")
            exclude("META-INF/versions/**")
            exclude("META-INF/services/**")
            exclude("META-INF/LICENSE")
            exclude("META-INF/LICENSE.txt")
            exclude("META-INF/NOTICE")
            exclude("META-INF/NOTICE.txt")

            // 运行时由平台提供或根本不需要
            exclude(dependency("com.google.code.findbugs:jsr305"))
            exclude(dependency("com.google.errorprone:error_prone_annotations"))
            exclude { it.moduleGroup == "com.google.code.gson" }
            exclude { it.moduleGroup == "com.google.guava" }
            exclude(dependency("com.google.j2objc:j2objc-annotations"))
            exclude { it.moduleGroup == "io.netty" }
            //exclude(dependency("io.netty:netty-all"))
            //exclude(dependency("io.netty:netty-buffer"))
            //exclude(dependency("io.netty:netty-codec"))
            //exclude(dependency("io.netty:netty-transport"))
            exclude(dependency("it.unimi.dsi:fastutil"))
            exclude(dependency("org.checkerframework:checker-qual"))
            exclude(dependency("org.jspecify:jspecify"))
            exclude(dependency("org.slf4j:slf4j-api"))
        }

        fun relocate0(pattern: String, result: String) {
            relocate(pattern, "cc.mewcraft.wakame.shaded.$result")
        }

        // shadow
        relocate0("me.lucko.shadow", "shadow")
        // mocha
        relocate0("team.unnamed.mocha", "mocha")
        // fleks
        relocate0("com.github.quillraven.fleks", "fleks")
        // configurate
        relocate0("org.spongepowered.configurate", "configurate")
        // commons
        relocate0("xyz.xenondevs.commons", "xenoncommons")
        // creative
        relocate0("team.unnamed.creative", "creative")
        // cloud
        relocate0("org.incendo.cloud", "cloud")
        // messenger
        relocate0("ninja.egg82.messenger", "messenger")
        // geantyref
        relocate0("io.leangen.geantyref", "geantyref")
        // reflection remapper
        relocate0("xyz.jpenilla.reflectionremapper", "reflectionremapper")
        // tiny remapper
        relocate0("net.fabricmc.mappingio", "mappingio")
        // javassist (used by mocha)
        relocate0("javassist", "javassist")
        // nats (used by messenger-nats)
        relocate0("io.nats", "nats")
        // bouncy castle (used by nats)
        relocate0("org.bouncycastle", "bouncycastle")
        // redis (used by messenger-redis)
        relocate0("redis.clients", "redis.clients")
        // rabbitmq (used by messenger-rabbitmq)
        relocate0("com.rabbitmq", "rabbitmq")
        // json (used by rabbitmq)
        relocate0("org.json", "json")
        // zstd (used by messenger-redis)
        //relocate0("com.github.luben.zstd", "zstd") // FIXME https://pastes.dev/qEtUHGgkNT
        // caffeine (used by messenger)
        relocate0("com.github.benmanes.caffeine", "caffeine")
        // apache commons pool 2 (used by rabbitmq and jedis)
        relocate0("org.apache.commons.pool2", "apache.commons.pool2")
        // apache commons codec
        relocate0("org.apache.commons.codec", "apache.commons.codec")
        // jgit
        relocate0("org.eclipse.jgit", "jgit")
        // javaewah
        relocate0("com.googlecode.javaewah", "javaewah")
        relocate0("com.googlecode.javaewah32", "javaewah32")
        // exposed
        relocate0("org.jetbrains.exposed", "exposed")
        // hikari
        relocate0("com.zaxxer.hikari", "hikari")
        // mariadb
        relocate0("org.mariadb.jdbc", "mariadb.jdbc")
    }
}

repositories {
    // 在这里直接声明 repository 实际上违背了我们 Nyaadanbou 项目组的 conventions
    // 即, 所有 repositories 都应该由 cc.mewcraft.libraries-repository 这个 gradle 插件提供
    // 但为了方便, 就还是直接写在这里了, 以后也都尽量写在这里, 保持项目简洁
    configure()
}
