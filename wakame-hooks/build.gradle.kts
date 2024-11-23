plugins {
    id("nyaadanbou-conventions.repositories")
    id("wakame-conventions.kotlin")
    id("wakame-conventions.koin")
}

version = "0.0.1"

dependencies {
    // internal
    compileOnly(project(":wakame-api"))
    compileOnly(project(":wakame-common"))
    compileOnly(platform(libs.bom.caffeine))

    // libraries
    compileOnly(local.paper)

    // plugins
    compileOnly(local.helper)
    compileOnly(local.mythicmobs) { isTransitive = false /* we don't want trash from the MM jar */ }
    compileOnly(local.adventurelevel)
    compileOnly(local.economy)
    compileOnly(local.chestsort) { isTransitive = false }
}
