plugins {
    id("koish-conventions.kotlin")
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
    compileOnly(platform(libs.bom.exposed))

    // libraries
    compileOnly(local.paper)
    compileOnly(local.towny) { isTransitive = false }
}
