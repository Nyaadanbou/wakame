plugins {
    id("koish-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
    maven {
        name = "jsinco"
        url = uri("https://repo.jsinco.dev/releases")
    }
}

dependencies {
    // master
    compileOnly(local.breweryx)

    // internal
    compileOnly(project(":wakame-mixin"))
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.paper)
    compileOnly(platform(libs.bom.caffeine))
    compileOnly(platform(libs.bom.configurate.yaml))
    compileOnly(local.commons.provider)
}
