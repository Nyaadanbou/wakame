plugins {
    id("neko-kotlin")
    id("nyaadanbou-conventions.repositories")
}

dependencies {
    // jgit
    compileOnly(platform(libs.bom.jgit))
}
