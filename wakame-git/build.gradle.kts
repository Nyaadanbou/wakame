plugins {
    id("nyaadanbou-conventions.repositories")
    id("wakame-conventions.kotlin")
    id("wakame-conventions.koin")
}

version = "0.0.1"

dependencies {
    // server
    compileOnly(local.paper)
    // jgit
    compileOnly(platform(libs.bom.jgit))
}
