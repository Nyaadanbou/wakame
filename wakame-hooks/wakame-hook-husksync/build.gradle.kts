plugins {
    id("koish.core-hook-conventions")
}

version = "0.0.1"

repositories {
    maven {
        name = "william278"
        url = uri("https://repo.william278.net/releases")
    }
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.husksync)
}
