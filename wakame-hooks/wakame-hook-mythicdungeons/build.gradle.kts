plugins {
    id("koish.core-hook-conventions")
}

version = "0.0.1"

repositories {
    //maven { // FIXME 等 MythicDungeons 仓库恢复后使用在线依赖
    //    name = "aestrusReleases"
    //    url = uri("https://maven.aestrus.io/releases")
    //}
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries
    //compileOnly(local.mythicdungeons) // FIXME 等 MythicDungeons 仓库恢复后使用在线依赖
    compileOnly(files("libs/MythicDungeons.jar"))
}
