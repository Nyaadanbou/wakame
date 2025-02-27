plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.mewcraft.cc/private") {
        credentials {
            username = providers.gradleProperty("nyaadanbou.mavenUsername").orNull
            password = providers.gradleProperty("nyaadanbou.mavenPassword").orNull
        }
    }
}

gradlePlugin {
    plugins {
        create("build-copy") {
            id = "cc.mewcraft.build-copy"
            implementationClass = "BuildCopyPlugin"
        }
        create("docker-copy") {
            id = "cc.mewcraft.docker-copy"
            implementationClass = "DockerCopyPlugin"
        }
    }
}

dependencies {
    implementation(local.plugin.nyaadanbou.conventions)
    implementation(local.plugin.kotlin.jvm)
    implementation(local.plugin.paperweight.userdev)
    implementation(local.docker.java)
    implementation(local.apache.commons.compress)
    implementation(libs.shadow)
    implementation(libs.indra.common)
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(files(local.javaClass.superclass.protectionDomain.codeSource.location))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}