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
}
