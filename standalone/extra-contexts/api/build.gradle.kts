plugins {
    id("koish.extracontexts-conventions")
    `maven-publish`
}

group = "cc.mewcraft.extracontexts"
version = "0.0.1-snapshot"

publishing {
    repositories {
        nyaadanbouReleases()
    }
    publications {
        create<MavenPublication>("maven") {
            artifactId = "api"
            from(components["java"])
        }
    }
}
