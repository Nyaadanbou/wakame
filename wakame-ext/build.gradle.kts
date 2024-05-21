plugins {
    id("neko-kotlin")
    id("neko.repositories") version "1.0-SNAPSHOT"
}

dependencies {
    // server
    compileOnly(libs.server.paper)

    // helper
    compileOnly(libs.helper)

    // internal
    compileOnly(platform(libs.bom.caffeine))
    compileOnly(project(":wakame-common"))

    // external
    compileOnly(libs.jgit)
    compileOnly(libs.mythicmobs) {
        isTransitive = false // we don't want trash from the MM jar
    }
}
