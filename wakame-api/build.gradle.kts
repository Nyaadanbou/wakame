plugins {
    id("neko-java")
    id("nyaadanbou-conventions.repositories")
}

group = "cc.mewcraft.wakame"
version = "1.0.0-SNAPSHOT"
description = "Add custom stuff to server"

dependencies {
    compileOnly(project(":wakame-common"))
    compileOnly(local.paper)
    compileOnly(libs.checker.qual)
}
