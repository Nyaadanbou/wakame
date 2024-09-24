plugins {
    id("neko-kotlin")
    id("nyaadanbou-conventions.repositories")
}

dependencies {
    // server
    compileOnly(local.paper)
    // helper
    compileOnly(local.helper)
    compileOnly(local.shadow.nbt)
}
