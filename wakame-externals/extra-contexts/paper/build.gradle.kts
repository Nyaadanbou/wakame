plugins {
    id("koish.extracontexts-conventions")
    id("cc.mewcraft.libraries-repository")
}

version = "0.0.1"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

dependencies {
    implementation(project(":wakame-externals:extra-contexts:common"))
    compileOnly(local.paper)
    compileOnly(local.luckperms)
    implementation(platform(libs.bom.configurate.yaml))
    implementation(platform(libs.bom.configurate.extra.kotlin))
    implementation(platform(libs.bom.cloud.velocity))
    implementation(platform(libs.bom.cloud.kotlin)) {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
    }
}

tasks.shadowJar {
    archiveBaseName.set("ExtraContexts")
}
