plugins {
    `java-library`
}

// Expose version catalog
val local = the<org.gradle.accessors.dm.LibrariesForLocal>()

dependencies {
    implementation(local.koin.core) { exclude("org.jetbrains.kotlin") }
    testImplementation(local.koin.test) { exclude("org.jetbrains.kotlin") }
    testImplementation(local.koin.test.junit5) { exclude("org.jetbrains.kotlin") }
}
