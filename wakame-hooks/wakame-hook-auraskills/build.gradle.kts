plugins {
    id("koish-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.auraskills)
}
