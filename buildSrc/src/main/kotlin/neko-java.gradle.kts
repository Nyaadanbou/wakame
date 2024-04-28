plugins {
    id("net.kyori.indra")
    id("com.github.johnrengelman.shadow")
}

// TODO 1.20.5 - revert it when adventure 4.17.0 release is out
repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots"
    }
}

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
        jvmArgs("-XX:+EnableDynamicAgentLoading") // suppress Java agent warning
        useJUnitPlatform() // use JUnit 5
    }
}

java {
    withSourcesJar()
}

indra {
    javaVersions().target(21)
}
