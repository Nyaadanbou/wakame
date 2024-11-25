plugins {
    id("nyaadanbou-conventions.repositories")
    id("wakame-conventions.kotlin")
    id("wakame-conventions.koin")
    id("io.papermc.paperweight.userdev")
}

version = "0.0.1"

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries
    paperweight.paperDevBundle(local.versions.paper)
    compileOnly(local.mythicmobs) { isTransitive = false /* we don't want trash from the MM jar */ }
}
