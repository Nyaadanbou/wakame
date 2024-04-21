plugins {
    id("neko.repositories") version "1.0-SNAPSHOT"
    id("neko-kotlin")
}

group = "cc.mewcraft.wakame"
version = "1.0.0"
description = "Add custom stuff to server"

dependencies {
    // jgit
    implementation(platform(libs.bom.jgit))
}
