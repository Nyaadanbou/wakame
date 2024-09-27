val zipFileName = "wakame.zip"

tasks {
    register<Zip>("zipDatapack") {
        group = "build"
        from("datapack")
        archiveFileName = zipFileName
        destinationDirectory = layout.buildDirectory
    }
    register<Task>("syncDatapack") {
        group = "build"
        dependsOn("zipDatapack")
        doLast {
            val zipFile = layout.buildDirectory.file(zipFileName)
            val destination = (findProperty("copyDatapack.destination") as? String) ?: throw GradleException("No `copyDatapack.destination` defined")
            val args = arrayOf("rsync", "-avz", zipFile.get().asFile.path, destination)
            logger.lifecycle("Executing command line: ${args.joinToString(" ")}")
            exec { commandLine(*args) }
        }
    }
}
