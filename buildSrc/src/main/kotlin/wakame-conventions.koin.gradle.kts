plugins {
    `java-library`
}

// Expose version catalog
val local = the<org.gradle.accessors.dm.LibrariesForLocal>()

dependencies {
    implementation(platform(local.koin.bom))
    implementation(local.koin.core) {
        exclude("org.jetbrains.kotlin")
    }
    implementation(local.koin.core.coroutines) {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
    }

    testImplementation(local.koin.test) { exclude("org.jetbrains.kotlin") }
    testImplementation(local.koin.test.junit5) { exclude("org.jetbrains.kotlin") }
}
