plugins {
    id("neko-java")
    id("neko.repositories") version "1.0-SNAPSHOT"
}

group = "cc.mewcraft.wakame"
version = "1.0.0-SNAPSHOT"
description = "Add custom stuff to server"

dependencies {
    compileOnly(project(":wakame-common"))
    compileOnly(libs.server.paper) // TODO 1.20.5 - revert it when purpur is out
    compileOnly(libs.checker.qual)
}
