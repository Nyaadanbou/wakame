plugins {
    id("neko-kotlin")
    id("neko-koin")
    id("nyaadanbou-conventions.repositories")
}

version = "0.0.1"

dependencies {
    // internal
    compileOnly(project(":wakame-api"))
    compileOnly(project(":wakame-common"))
    compileOnly(platform(libs.bom.caffeine))
    compileOnly(local.paper)
    compileOnly(local.helper)

    // external
    compileOnly(local.mythicmobs) { isTransitive = false /* we don't want trash from the MM jar */ }
    compileOnly(local.adventurelevel)
}
