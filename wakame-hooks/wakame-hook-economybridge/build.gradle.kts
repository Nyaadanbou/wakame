plugins {
    id("koish.core-conventions")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
    maven {
        name = "nightexpress-releases"
        url = uri("https://repo.nightexpressdev.com/releases")
    }
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.nightcore)
    compileOnly(local.economybridge)
    compileOnly(local.economy) { isTransitive = false } // 我们使用 Koish 让 Economy 去兼容 EeconomyBridge
}
