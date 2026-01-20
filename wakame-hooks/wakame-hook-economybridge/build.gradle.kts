plugins {
    id("koish.core-hook-conventions")
}

version = "0.0.1"

repositories {
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
