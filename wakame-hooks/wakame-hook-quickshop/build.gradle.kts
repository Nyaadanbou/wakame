plugins {
    id("wakame-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

dependencies {
    // internal
    compileOnly(project(":wakame-mixin"))
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.paper)
    compileOnly(local.quickshop.api) { isTransitive = false }
}
