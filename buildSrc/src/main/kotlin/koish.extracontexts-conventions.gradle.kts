plugins {
    id("koish.kotlin-conventions")
}

configurations.all {
    exclude("org.spongepowered", "configurate-core")
    exclude("org.spongepowered", "configurate-yaml")
    exclude("org.spongepowered", "configurate-gson")
}

configurations.runtimeClasspath {
    exclude("org.jetbrains.kotlin")
    exclude("org.jetbrains.kotlinx")
}

tasks {
    shadowJar {
        archiveClassifier.set("shaded")

        mergeServiceFiles()
        // Needed for mergeServiceFiles to work properly in Shadow 9+
        filesMatching("META-INF/services/**") {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        dependencies {

            // 没啥用的元数据文件
            exclude("META-INF/maven/**")
            exclude("META-INF/licenses/**")
            exclude("META-INF/versions/**")
            exclude("**/INFO_BIN")
            exclude("**/INFO_SRC")
            exclude("**/LICENSE")
            exclude("**/README")

            // 运行时由平台提供或根本不需要
            exclude(dependency("com.google.code.findbugs:jsr305"))
            exclude(dependency("com.google.errorprone:error_prone_annotations"))
            exclude { it.moduleGroup == "com.google.code.gson" }
            exclude { it.moduleGroup == "com.google.guava" }
            exclude(dependency("com.google.j2objc:j2objc-annotations"))
            exclude { it.moduleGroup == "io.netty" }
            exclude(dependency("it.unimi.dsi:fastutil"))
            exclude(dependency("org.checkerframework:checker-qual"))
            exclude(dependency("org.jspecify:jspecify"))
            exclude(dependency("org.slf4j:slf4j-api"))
        }

        fun relocate0(pattern: String, result: String) {
            relocate(pattern, "cc.mewcraft.extracontexts.shaded.$result")
        }

        // configurate
        relocate0("org.spongepowered.configurate", "configurate")
        // cloud
        relocate0("org.incendo.cloud", "cloud")
        // messenger
        relocate0("ninja.egg82.messenger", "messenger")
        // geantyref
        relocate0("io.leangen.geantyref", "geantyref")
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
        // javaewah
        relocate0("com.googlecode.javaewah", "javaewah")
        relocate0("com.googlecode.javaewah32", "javaewah32")
        // protobuf
        relocate0("com.google.protobuf", "protobuf")
        // exposed
        relocate0("org.jetbrains.exposed", "exposed")
        // hikari
        relocate0("com.zaxxer.hikari", "hikari")
        // mariadb
        relocate0("org.mariadb.jdbc", "mariadb.jdbc")
        // mysql
        relocate0("com.mysql.cj", "mysql.cj")
        relocate0("com.mysql.jdbc", "mysql.jdbc")
        // postgresql
        relocate0("org.postgresql", "postgresql")
        // h2
        relocate0("org.h2", "h2")
    }
}
