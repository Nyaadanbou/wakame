plugins {
    id("koish.core-hook-conventions")
}

version = "0.0.1"

repositories {
    maven {
        name = "jsinco"
        url = uri("https://repo.jsinco.dev/releases")
    }
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.breweryx)
}
