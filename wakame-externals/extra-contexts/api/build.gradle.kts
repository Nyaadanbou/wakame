plugins {
    id("koish.extracontexts-conventions")
    id("cc.mewcraft.libraries-repository")
    `maven-publish`
}

group = "cc.mewcraft.extracontexts"
version = "0.0.1-snapshot"

repositories {
    nyaadanbouReleases()
    nyaadanbouPrivate()
}

publishing {
    repositories {
        nyaadanbouPrivate().apply {
            credentials(PasswordCredentials::class)
        }
    }
    publications {
        create<MavenPublication>("maven") {
            artifactId = "api"
            from(components["java"])
        }
    }
}
