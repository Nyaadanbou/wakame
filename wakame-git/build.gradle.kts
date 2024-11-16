plugins {
    id("neko-kotlin")
    id("neko-koin")
    id("nyaadanbou-conventions.repositories")
}

version = "0.0.1"

dependencies {
    // server
    compileOnly(local.paper)
    // jgit
    compileOnly(platform(libs.bom.jgit))
}
