plugins {
    id("koish.core-hook-conventions")
    id("io.canvasmc.weaver.userdev")
}

version = "0.0.1"

dependencies {
    paperweight.paperDevBundle(local.versions.paper)

    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.mythicmobs) { isTransitive = false /* we don't want trash from the MM jar */ }
}
