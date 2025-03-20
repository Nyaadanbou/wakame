plugins {
    id("nyaadanbou-repository") // 根目录的 settings.gradle.kts 已经声明过, 所以这里必须省略 version
}

dependencyResolutionManagement {
    versionCatalogs {
        create("local") {
            from(files("../gradle/local.versions.toml"))
        }
    }
    versionCatalogs {
        create("libs") {
            from("cc.mewcraft.gradle:catalog:0.8-SNAPSHOT")
        }
    }
}
