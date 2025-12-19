plugins {
    id("koish-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouPrivate()
}

dependencies {
    // internal
    compileOnly(project(":wakame-mixin"))
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.paper)
    compileOnly(local.betterhud.api) { isTransitive = false }
    compileOnly(local.betterhud.bukkit) { isTransitive = false }
    compileOnly(local.bettercommand) { isTransitive = false }
}
