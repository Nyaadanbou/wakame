plugins {
    id("koish.core-hook-conventions")
}

version = "0.0.1"

repositories {
    // plugin: Portals
    maven {
        name = "thenextlvlReleases"
        url = uri("https://repo.thenextlvl.net/releases")
    }
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // plugin: Portals
    compileOnly("net.thenextlvl:portals:1.4.0")
}
