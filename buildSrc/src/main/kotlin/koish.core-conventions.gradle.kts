plugins {
    id("koish.kotlin-conventions")
    id("nyaadanbou-repository-project")
    id("cc.mewcraft.libraries-repository")
    id("xyz.jpenilla.gremlin-gradle")
}

repositories {
    // 在这里直接声明 repository 实际上违背了我们 Nyaadanbou 项目组的 conventions
    // 即, 所有 repositories 都应该由 cc.mewcraft.libraries-repository 这个 gradle 插件提供
    // 但为了方便, 就还是直接写在这里了, 以后也都尽量写在这里, 保持项目简洁
    jmpSnapshots()
}

gremlin {
    defaultJarRelocatorDependencies = false
    defaultGremlinRuntimeDependency = false
}

tasks {
    writeDependencies {
        outputFileName = "koish-dependencies.txt"
        repos.add("https://repo.papermc.io/repository/maven-public/")
        repos.add("https://repo.maven.apache.org/maven2/")
        repos.add("https://repo.mewcraft.cc/releases")
        repos.add("https://repo.xenondevs.xyz/releases")
        repos.add("https://repo.jpenilla.xyz/snapshots")
        repos.add("https://repo.nexomc.com/releases")
    }
    relocateWithPrefix("koish.libs") {
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