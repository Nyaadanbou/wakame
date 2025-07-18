plugins {
    id("wakame-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouPrivate()
}

dependencies {
    // internal
    compileOnly(project(":wakame-api"))
    compileOnly(project(":wakame-mixin"))
    compileOnly(project(":wakame-plugin"))
    compileOnly(local.mongodb.driver)
    compileOnly(local.mongodb.bson)
    compileOnly(local.fleks) {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
    }
    compileOnly(platform(libs.bom.invui)) /* 由自定义的 classloader 加载 */ {
        exclude("org.jetbrains")
    }

    // libraries
    compileOnly(local.paper)
    compileOnly(local.towny) { isTransitive = false }
}
