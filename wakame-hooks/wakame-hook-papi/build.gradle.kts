plugins {
    id("wakame-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
    id("io.papermc.paperweight.userdev")
}

version = "0.0.1"

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))
    compileOnly(project(":wakame-mixin"))
    compileOnly(local.fleks) {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
    }

    // libraries
    paperweight.paperDevBundle(local.versions.paper)
    compileOnly(local.papi) { isTransitive = false }
}
