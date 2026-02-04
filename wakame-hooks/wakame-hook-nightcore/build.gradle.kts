plugins {
    id("koish.core-hook-conventions")
}

version = "0.0.1"

repositories {
    // plugin: nightcore
    maven {
        name = "nightexpressReleases"
        url = uri("https://repo.nightexpressdev.com/releases")
    }
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // plugin: economy
    compileOnly(local.economy)

    // plugin: nightcore
    compileOnly("su.nightexpress.nightcore:main:2.13.3")
}
