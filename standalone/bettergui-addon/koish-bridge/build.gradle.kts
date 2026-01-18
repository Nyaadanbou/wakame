plugins {
    id("koish.bettergui-addon")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

dependencies {
    // internal
    compileOnly(project(":wakame-plugin"))
}

tasks {
    shadowJar {
        archiveBaseName.set("KoishBridge")
    }
}