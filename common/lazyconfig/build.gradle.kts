plugins {
    id("koish.kotlin-conventions")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

dependencies {
    api(platform(libs.bom.configurate.yaml))
    api(platform(libs.bom.configurate.extra.kotlin))
    api(platform(libs.bom.cloud.velocity))
    api(platform(libs.bom.cloud.kotlin)) {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
    }
}
