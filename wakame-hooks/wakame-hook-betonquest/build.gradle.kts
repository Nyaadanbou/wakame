plugins {
    id("koish-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
    maven {
        name = "betonquestRepo"
        url = uri("https://nexus.betonquest.org/repository/betonquest/")
    }
    //maven { // FIXME 等 MythicDungeons 仓库恢复后使用在线依赖
    //    name = "aestrusReleases"
    //    url = uri("https://maven.aestrus.io/releases")
    //}
}

dependencies {
    // internal
    compileOnly(project(":wakame-mixin"))
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.betonquest) { isTransitive = false}
    //compileOnly(local.mythicdungeons) // FIXME 等 MythicDungeons 仓库恢复后使用在线依赖
    compileOnly(files("libs/MythicDungeons.jar"))
}
