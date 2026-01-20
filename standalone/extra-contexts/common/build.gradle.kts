plugins {
    id("koish.extracontexts-conventions")
}

version = "0.0.1"

dependencies {
    api(project(":standalone:extra-contexts:api"))
    api(project(":common:messaging"))

    api(local.gremlin.runtime)
    api(local.jarrelocator)

    compileOnly(local.luckperms)

    compileOnly(local.caffeine)
    compileOnly(local.exposed.core)
    compileOnly(local.exposed.dao)
    compileOnly(local.exposed.jdbc)
    compileOnly(local.h2)
    compileOnly(local.mariadb.jdbc)
    compileOnly(local.mysql.jdbc)
    compileOnly(local.postgresql.jdbc)
    compileOnly(local.hikaricp)
}