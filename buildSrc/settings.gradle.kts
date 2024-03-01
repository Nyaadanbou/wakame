@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositories {
        maven(uri("${System.getProperty("user.home")}/MewcraftRepository"))
    }
    versionCatalogs {
        create("libs") {
            from("cc.mewcraft.gradle:catalog:1.0")
        }
    }
}
