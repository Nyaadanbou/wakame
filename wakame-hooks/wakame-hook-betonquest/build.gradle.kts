plugins {
    id("koish.core-conventions")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()

    // plugin: BetonQuest
    maven {
        name = "betonquest"
        url = uri("https://repo.betonquest.org/betonquest/")
    }

    // plugin: TheBrewingProject
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }

    // plugin: MythicDungeons
    //maven { // FIXME 等 MythicDungeons 仓库恢复后使用在线依赖
    //    name = "aestrusReleases"
    //    url = uri("https://maven.aestrus.io/releases")
    //}

    // plugin: PlotSquared
    maven {
        name = "codemcReleases"
        url = uri("https://repo.codemc.io/repository/maven-releases/")
    }
    maven {
        name = "codemcSnapshots"
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))

    // libraries

    // plugin: BetonQuest
    compileOnly(local.betonquest) {
        exclude("de.themoep")
    }

    // plugin: TheBrewingProject
    compileOnly(local.thebrewingproject) { isTransitive = false }

    // plugin: MythicDungeons
    //compileOnly(local.mythicdungeons) // FIXME 等 MythicDungeons 仓库恢复后使用在线依赖
    compileOnly(files("libs/MythicDungeons.jar"))

    // plugin: PlotSquared
    implementation(platform("com.intellectualsites.bom:bom-newest:1.47"))
    compileOnly("com.intellectualsites.plotsquared:plotsquared-core")
    compileOnly("com.intellectualsites.plotsquared:plotsquared-bukkit") { isTransitive = false }

    // plugin: ExtraContexts
    compileOnly(project(":wakame-externals:extra-contexts:api"))
}
