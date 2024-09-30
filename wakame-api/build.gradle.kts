plugins {
    id("neko-kotlin")
    id("nyaadanbou-conventions.repositories")
}

group = "cc.mewcraft.wakame"
version = "1.0.0"
description = "The API of the core system"

dependencies {
    compileOnly(project(":wakame-common")) // 运行时由服务端提供
    compileOnly(local.paper)
    compileOnly(libs.checker.qual)
}
