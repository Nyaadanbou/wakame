plugins {
    id("nyaadanbou-repository-settings") // 根目录的 settings.gradle.kts 已经声明过, 所以这里必须省略 version
}

dependencyResolutionManagement {
    versionCatalogs {
        create("local") {
            from(files("../gradle/local.versions.toml"))
        }
    }
}
