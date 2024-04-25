plugins {
    id("neko-kotlin")
    id("neko.repositories") version "1.0-SNAPSHOT"
}

dependencies {
    // jgit
    compileOnly(platform(libs.bom.jgit))
}
