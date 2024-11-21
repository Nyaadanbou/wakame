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
        // suppress Java agent warning
        jvmArgs("-XX:+EnableDynamicAgentLoading")
        // use JUnit 5
        useJUnitPlatform()
    }
}

java {
    withSourcesJar()
}

indra {
    javaVersions().target(21)
}
