plugins {
    id("koish.core-hook-conventions")
}

version = "0.0.1"

repositories {
    // plugin: HibiscusCommons
    maven {
        name = "hibiscusReleases"
        url = uri("https://repo.hibiscusmc.com/releases")
    }
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // plugin: HibiscusCommons (所有版本: https://repo.hibiscusmc.com/#/releases/me/lojosho/HibiscusCommons)
    compileOnly("me.lojosho:HibiscusCommons:0.8.2-f3f79539")
}
