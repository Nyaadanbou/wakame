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
    implementation("io.insert-koin", "koin-core", Versions.KoinCore) {
        exclude("org.jetbrains.kotlin")
    }
    implementation("io.insert-koin", "koin-annotations", Versions.KoinAnnotations) {
        exclude("org.jetbrains.kotlin")
    }
    ksp("io.insert-koin", "koin-ksp-compiler", Versions.KoinKsp)

    // test
    testImplementation("io.insert-koin", "koin-test", Versions.KoinCore) {
        exclude("org.jetbrains.kotlin")
    }
    testImplementation("io.insert-koin", "koin-test-junit4", Versions.KoinCore) {
        exclude("org.jetbrains.kotlin")
    }
}
