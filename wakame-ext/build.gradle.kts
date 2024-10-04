plugins {
    id("neko-kotlin")
    id("nyaadanbou-conventions.repositories")
}

version = "0.0.1"

dependencies {
    // server
    compileOnly(local.paper)

    // helper
    compileOnly(local.helper)

    // internal
    compileOnly(platform(libs.bom.caffeine))
    compileOnly(project(":wakame-common"))

    // external
    compileOnly(libs.jgit)
    compileOnly(libs.mythicmobs) { isTransitive = false /* we don't want trash from the MM jar */ }
    compileOnly(libs.adventurelevel)
}
