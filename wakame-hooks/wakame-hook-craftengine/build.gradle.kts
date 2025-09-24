plugins {
    id("koish-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
    repositories {
        maven {
            name = "momirealms"
            url = uri("https://repo.momirealms.net/releases/")
        }
    }
}

dependencies {
    // internal
    compileOnly(project(":wakame-mixin"))
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.craftengine.core)
    compileOnly(local.craftengine.bukkit)
}
