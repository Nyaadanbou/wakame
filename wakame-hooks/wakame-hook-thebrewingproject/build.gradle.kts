plugins {
    id("koish.core-hook-conventions")
}

version = "0.0.1"

repositories {
    // plugin: TheBrewingProject
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.thebrewingproject)
    compileOnly(local.mythicmobs) { isTransitive = false /* we don't want trash from the MM jar */ }
}
