plugins {
    id("koish.kotlin-conventions")
    id("nyaadanbou-repository-project")
    id("cc.mewcraft.libraries-repository")
}

val local = the<org.gradle.accessors.dm.LibrariesForLocal>()

repositories {
    maven {
        name = "codeMC"
        url = uri("https://repo.codemc.org/repository/maven-public")
    }
    maven {
        name = "sonatypeOssPublic"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly(local.paper)
    compileOnly(local.bettergui)
}

tasks {
    shadowJar {
        archiveClassifier.set("shaded")
        excludeMiscellaneousFiles()
        relocate("me.hsgamer.hscore.license", "me.hsgamer.bettergui.lib.license")
        relocate("me.hsgamer.hscore", "me.hsgamer.bettergui.lib.core")
        relocate("io.github.projectunified.minelib", "me.hsgamer.bettergui.lib.minelib")
        relocate("org.bstats", "me.hsgamer.bettergui.lib.bstats")
    }
}