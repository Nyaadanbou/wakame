plugins {
    id("net.kyori.indra")
    id("net.kyori.indra.checkstyle")
    id("com.diffplug.spotless")
    id("com.gradleup.shadow")
}

val local = the<org.gradle.accessors.dm.LibrariesForLocal>()

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    compileTestJava {
        options.encoding = "UTF-8"
    }
    assemble {
        dependsOn(shadowJar)
    }
    test {
        // suppress Java agent warning
        jvmArgs("-XX:+EnableDynamicAgentLoading")
        // use JUnit 5
        useJUnitPlatform()
    }
    shadowJar {
        configure()
    }
}

java {
    withSourcesJar()
}

indra {
    checkstyle().set(local.versions.checkstyle)
    javaVersions().target(21)
}

spotless {
    java {
        googleJavaFormat()
        applyCommon()
        importOrderFile(".spotless/wakame.importorder")
    }
}

dependencies {
    checkstyle(local.stylecheck)
}
