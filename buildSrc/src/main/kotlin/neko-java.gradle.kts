plugins {
    id("net.kyori.indra")
    id("com.gradleup.shadow")
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
