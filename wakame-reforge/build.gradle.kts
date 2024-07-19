plugins {
    id("neko-kotlin")
    id("neko.repositories") version "1.0-SNAPSHOT"
}

dependencies {
    compileOnly(libs.server.paper)
    implementation(platform(libs.bom.invui))
}
