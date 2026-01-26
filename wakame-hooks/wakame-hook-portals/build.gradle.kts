plugins {
    id("koish.core-hook-conventions")
}

version = "0.0.1"

repositories {
    // plugin: Portals
    maven {
        name = "jitPack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries

    // plugin: Portals
    compileOnly("com.github.TheNextLvl-net:portals:1.1.5") {
        isTransitive = false
    }
}
