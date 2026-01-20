plugins {
    id("koish.core-hook-conventions")
}

version = "0.0.1"

repositories {
    maven {
        name = "momirealms"
        url = uri("https://repo.momirealms.net/releases/")
    }
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.craftengine.core)
    compileOnly(local.craftengine.bukkit)
}
