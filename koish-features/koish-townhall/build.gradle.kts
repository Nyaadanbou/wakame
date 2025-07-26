plugins {
    id("wakame-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

dependencies {
    // internal
    compileOnly(project(":wakame-api"))
    compileOnly(project(":wakame-mixin"))
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.mongodb.driver)
    compileOnly(local.mongodb.bson)
    compileOnly(local.fleks) {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
    }
    compileOnly(platform(libs.bom.invui)) {
        exclude("org.jetbrains")
    }
    compileOnly(local.paper)
    compileOnly(local.towny) { isTransitive = false }
    compileOnly(platform(libs.bom.configurate.yaml))
    compileOnly(platform(libs.bom.configurate.extra.kotlin))
}
