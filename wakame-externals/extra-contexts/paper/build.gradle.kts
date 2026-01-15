plugins {
    id("koish.extracontexts-conventions")
    id("cc.mewcraft.libraries-repository")
    id("cc.mewcraft.copy-jar-docker")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

dependencies {
    implementation(project(":wakame-externals:extra-contexts:common"))

    compileOnly(local.paper)
}

dockerCopy {
    containerId = "aether-minecraft-1"
    containerPath = "/minecraft/game1/plugins/"
    fileMode = 0b110_100_100
    userId = 999
    groupId = 999
    archiveTask = "shadowJar"
}

tasks {
    shadowJar {
        archiveBaseName.set("ExtraContexts")
        val pkg = "cc.mewcraft.extracontexts"
        configureForPlatform(pkg, ServerPlatform.PAPER)
        relocateWithPrefix(pkg) {
            //move("com.github.luben.zstd", "zstd") // relocations don't work with natives. FIXME https://pastes.dev/qEtUHGgkNT
            move("cc.mewcraft.lazyconfig", "lazyconfig")
            move("cc.mewcraft.messaging2", "messaging2")
            move("com.github.benmanes.caffeine", "caffeine")
            move("com.google.protobuf", "protobuf")
            move("com.googlecode.javaewah", "javaewah")
            move("com.googlecode.javaewah32", "javaewah32")
            move("com.mysql.cj", "mysql.cj")
            move("com.mysql.jdbc", "mysql.jdbc")
            move("com.rabbitmq", "rabbitmq")
            move("com.zaxxer.hikari", "hikari")
            move("io.leangen.geantyref", "geantyref")
            move("io.nats", "nats")
            move("ninja.egg82.messenger", "messenger")
            move("org.apache.commons.codec", "apache.commons.codec")
            move("org.apache.commons.pool2", "apache.commons.pool2")
            move("org.bouncycastle", "bouncycastle")
            move("org.h2", "h2")
            move("org.incendo.cloud", "cloud")
            move("org.jetbrains.exposed", "exposed")
            move("org.json", "json")
            move("org.mariadb.jdbc", "mariadb.jdbc")
            move("org.postgresql", "postgresql")
            move("org.spongepowered.configurate", "configurate")
            move("redis.clients", "redis.clients")
            move("xyz.xenondevs.commons", "xenoncommons")
        }
    }
}