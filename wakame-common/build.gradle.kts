plugins {
    id("neko-kotlin")
    id("neko.repositories") version "1.0-SNAPSHOT"
}

dependencies {
    // server
    compileOnly(local.paper)
    // helper
    compileOnly(local.helper)
    compileOnly(libs.shadow.nbt)
}
