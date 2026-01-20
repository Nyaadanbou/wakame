plugins {
    id("koish.extracontexts-conventions")
    id("cc.mewcraft.copy-jar-docker")
}

version = "0.0.1"

dependencies {
    implementation(project(":standalone:extra-contexts:common"))
    compileOnly(local.luckperms)
    compileOnly(local.velocity.api); kapt(local.velocity.api)

    koishLoader(local.commons.collections)
    koishLoader(local.commons.gson)
    koishLoader(local.commons.provider)
    koishLoader(local.commons.reflection)
    koishLoader(local.commons.tuple)
    koishLoader(local.configurate.yaml)
    koishLoader(local.configurate.gson)
    koishLoader(local.configurate.extra.dfu8)
    koishLoader(local.configurate.extra.kotlin)
    koishLoader(local.fastutil) // 与 paper 不同: velocity 运行时并不包含此依赖, 但 messaging 需要这个
    koishLoader(local.messenger)
    koishLoader(local.messenger.nats)
    koishLoader(local.messenger.rabbitmq)
    koishLoader(local.messenger.redis)
    koishLoader(local.zstdjni)
    koishLoader(local.jedis)
    koishLoader(local.rabbitmq)
    koishLoader(local.nats)
    koishLoader(local.caffeine)
    koishLoader(local.exposed.core)
    koishLoader(local.exposed.dao)
    koishLoader(local.exposed.jdbc)
    koishLoader(local.h2)
    koishLoader(local.mariadb.jdbc)
    koishLoader(local.mysql.jdbc)
    koishLoader(local.postgresql.jdbc)
    koishLoader(local.hikaricp)
}

sourceSets {
    main {
        blossom {
            configure(project)
        }
    }
}

dockerCopy {
    containerId = "aether-minecraft-1"
    containerPath = "/minecraft/proxy/plugins/"
    fileMode = 0b110_100_100
    userId = 999
    groupId = 999
    archiveTask = "shadowJar"
}

tasks {
    shadowJar {
        archiveBaseName.set("extracontexts")
        configure(ServerPlatform.VELOCITY)
    }
    relocateWithPrefix("extracontexts.libs") {
        moveFastutil()
    }
}

configurations {
    runtimeDownload {
        excludePlatformRuntime(ServerPlatform.VELOCITY)
    }
}