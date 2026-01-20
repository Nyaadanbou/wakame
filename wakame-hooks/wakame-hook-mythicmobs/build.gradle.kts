plugins {
    id("koish.core-hook-conventions")
}

version = "0.0.1"

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries
    compileOnly(local.mythicmobs) { isTransitive = false /* we don't want trash from the MM jar */ }
}
