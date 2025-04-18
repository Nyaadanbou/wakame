plugins {
    id("wakame-conventions.kotlin")
    id("cc.mewcraft.libraries-repository")
    id("io.papermc.paperweight.userdev")
}

version = "0.0.1"

repositories {
        nyaadanbouPrivate()
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries
    paperweight.paperDevBundle(local.versions.paper)
    compileOnly(local.mythicmobs) { isTransitive = false /* we don't want trash from the MM jar */ }
    compileOnly(platform(libs.bom.caffeine))
}
