plugins {
    id("koish.core-hook-conventions")
}

version = "0.0.1"

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.betterhud.api) { isTransitive = false }
    compileOnly(local.betterhud.bukkit) { isTransitive = false }
    compileOnly(local.bettercommand) { isTransitive = false }
}
