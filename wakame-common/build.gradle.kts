// project description:
// 该项目是所有其他项目 (mixin, plugin, ...) 的共同依赖.

plugins {
    id("nyaadanbou-conventions.repositories")
    id("wakame-conventions.kotlin")
    `maven-publish`
}

group = "cc.mewcraft.wakame"
version = "0.0.1"

dependencies {
    compileOnly(local.paper)
    compileOnly(local.shadow.nbt)
}
