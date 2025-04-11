plugins {
    id("wakame-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
    id("io.papermc.paperweight.userdev")
}

version = "0.0.1"

repositories {
    nyaadanbouPrivate()
    maven {
        name = "nightexpress-releases"
        url = uri("https://repo.nightexpressdev.com/releases")
    }
}

dependencies {
    // internal
    compileOnly(project(":wakame-mixin"))
    compileOnly(project(":wakame-plugin"))

    // libraries
    paperweight.paperDevBundle(local.versions.paper)
    compileOnly(local.nightcore)
    compileOnly(local.economybridge)
}
