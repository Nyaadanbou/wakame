plugins {
    id("neko-kotlin")
    id("neko.repositories") version "1.0-SNAPSHOT"
}

dependencies {
    compileOnly(local.paper)
    implementation(platform(libs.bom.invui))
}
