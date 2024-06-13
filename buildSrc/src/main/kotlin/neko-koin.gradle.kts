plugins {
    `java-library`
    id("com.google.devtools.ksp")
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

sourceSets.test {
    java.srcDirs("build/generated/ksp/test/kotlin")
}

dependencies {
    // production
    implementation("io.insert-koin", "koin-core", Versions.KOIN_CORE) {
        exclude("org.jetbrains.kotlin")
    }
    implementation("io.insert-koin", "koin-annotations", Versions.KOIN_ANNOTATIONS) {
        exclude("org.jetbrains.kotlin")
    }
    ksp("io.insert-koin", "koin-ksp-compiler", Versions.KOIN_KSP)

    // test
    testImplementation("io.insert-koin", "koin-test", Versions.KOIN_CORE) {
        exclude("org.jetbrains.kotlin")
    }
    testImplementation("io.insert-koin", "koin-test-junit4", Versions.KOIN_CORE) {
        exclude("org.jetbrains.kotlin")
    }
}
