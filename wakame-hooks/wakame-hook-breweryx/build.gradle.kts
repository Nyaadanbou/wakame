plugins {
    id("koish.core-conventions")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
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
