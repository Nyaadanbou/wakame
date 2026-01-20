plugins {
    id("koish.core-hook-conventions")
}

version = "0.0.1"

repositories {
    // plugin: PlotSquared
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }

    // plugin: WorldEdit
    //maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries

    // plugin: PlotSquared
    implementation(platform("com.intellectualsites.bom:bom-newest:1.47"))
    compileOnly("com.intellectualsites.plotsquared:plotsquared-core")
    compileOnly("com.intellectualsites.plotsquared:plotsquared-bukkit") { isTransitive = false }

    // plugin: WorldEdit
    //compileOnly(local.worldedit)
}
