plugins {
    id("koish.kotlin-conventions")
    id("nyaadanbou-repository-project")
    id("cc.mewcraft.libraries-repository")
    id("xyz.jpenilla.gremlin-gradle")
}

repositories {
    jmpSnapshots()
}

gremlin {
    defaultJarRelocatorDependencies = false
    defaultGremlinRuntimeDependency = false
}

tasks {
    writeDependencies {
        outputFileName = "extracontexts-dependencies.txt"
        repos.add("https://repo.papermc.io/repository/maven-public/")
        repos.add("https://repo.maven.apache.org/maven2/")
        repos.add("https://repo.mewcraft.cc/releases")
        repos.add("https://repo.xenondevs.xyz/releases")
        repos.add("https://repo.jpenilla.xyz/snapshots")
    }
    relocateWithPrefix("extracontexts.libs") {
        moveConfigurate()
        moveGremlin()
        moveLazyConfig()
        moveMessaging()
    }
}

configurations {
    all {
        excludePlatformConfigurate()
    }
}
