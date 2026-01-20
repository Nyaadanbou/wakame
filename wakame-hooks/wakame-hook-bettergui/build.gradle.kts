plugins {
    id("koish.core-hook-conventions")
}

version = "0.0.1"

repositories {
    maven {
        name = "codemcMavenReleases"
        url = uri("https://repo.codemc.io/repository/maven-releases/")
    }
    maven {
        name = "codemcMavenSnapshots"
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.bettergui)
}
