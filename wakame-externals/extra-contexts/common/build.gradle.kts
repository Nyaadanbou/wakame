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
    api(project(":wakame-externals:extra-contexts:api"))
    compileOnly(local.luckperms)
    implementation(local.exposed.core)
    implementation(local.exposed.dao)
    implementation(local.exposed.jdbc)
    implementation(local.h2)
    implementation(local.mariadb.jdbc)
    implementation(local.mysql.jdbc)
    implementation(local.postgresql.jdbc)
    implementation(local.hikaricp)
}

