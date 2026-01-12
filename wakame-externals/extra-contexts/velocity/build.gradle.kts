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
    compileOnly(local.velocity.api); kapt(local.velocity.api)
    compileOnly(local.luckperms)
    implementation(platform(libs.bom.configurate.yaml))
    implementation(platform(libs.bom.configurate.extra.kotlin))
}

tasks.shadowJar {
    archiveBaseName.set("extracontexts")
}