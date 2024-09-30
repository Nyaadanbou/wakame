plugins {
    id("neko-kotlin")
    id("nyaadanbou-conventions.repositories")
}

version = "0.0.1"

dependencies {
    // jgit
    compileOnly(platform(libs.bom.jgit))
}
