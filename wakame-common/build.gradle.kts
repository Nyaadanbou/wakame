plugins {
    id("neko-kotlin")
    id("neko.repositories") version "1.0-SNAPSHOT"
}

dependencies {
    // server
    compileOnly(libs.server.paper)
    // helper
    compileOnly("me.lucko", "helper", "6.0.0-SNAPSHOT")
    compileOnly(libs.shadow.nbt)
}
