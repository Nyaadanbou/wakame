plugins {
    id("nyaadanbou-conventions.repositories")
    id("wakame-conventions.kotlin")
    id("io.papermc.paperweight.userdev")
}

version = "0.0.1"

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries
    paperweight.paperDevBundle(local.versions.paper)
    compileOnly(local.luckperms) { isTransitive = false }
}
