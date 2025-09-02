plugins {
    id("koish-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
    repositories {
        maven("https://jitpack.io")
    }
}

dependencies {
    compileOnly(files("libs/TheBrewingProject-1.9.0.jar"))

    // internal
    compileOnly(project(":wakame-mixin"))
    compileOnly(project(":wakame-plugin"))

    // libraries
    // compileOnly(local.paper)
    // compileOnly(platform(libs.bom.caffeine))
    // compileOnly(platform(libs.bom.configurate.yaml))
    // compileOnly(local.commons.provider)
}
