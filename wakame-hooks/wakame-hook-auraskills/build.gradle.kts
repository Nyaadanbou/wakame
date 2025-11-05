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
    compileOnly(project(":wakame-mixin"))
    compileOnly(project(":wakame-plugin"))

    // TODO 这个等移除 ECS 实现后就可以删掉了 (当初引入 ECS 真的只是为了学习)
    compileOnly(local.fleks) {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
    }

    // libraries
    compileOnly(local.auraskills)
}
