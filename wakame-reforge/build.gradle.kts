plugins {
    id("neko-kotlin")
    id("nyaadanbou-conventions.repositories")
}

dependencies {
    compileOnly(local.paper)
    implementation(platform(libs.bom.invui))
}
