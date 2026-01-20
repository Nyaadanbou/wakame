plugins {
    id("koish.bettergui-addon-conventions")
}

version = "0.0.1"

repositories {
    jmpSnapshots()
}

dependencies {
    compileOnly(project(":wakame-plugin"))
}

sourceSets {
    main {
        blossom {
            configure(project)
        }
    }
}

tasks {
    shadowJar {
        archiveBaseName.set("KoishBridge")
    }
}