plugins {
    `java-library`
}

dependencies {
    // production
    implementation("io.insert-koin", "koin-core", Versions.KOIN_CORE) {
        exclude("org.jetbrains.kotlin")
    }

    // test
    testImplementation("io.insert-koin", "koin-test", Versions.KOIN_CORE) {
        exclude("org.jetbrains.kotlin")
    }
    testImplementation("io.insert-koin", "koin-test-junit4", Versions.KOIN_CORE) {
        exclude("org.jetbrains.kotlin")
    }
}
