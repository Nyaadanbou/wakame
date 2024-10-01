// project description:
// 该项目是所有其他项目 (mixin, plugin, ...) 的共同依赖.

plugins {
    id("neko-kotlin")
    id("nyaadanbou-conventions.repositories")
}

version = "0.0.1"

dependencies {
    compileOnly(local.paper)
    compileOnly(local.shadow.nbt)
}
