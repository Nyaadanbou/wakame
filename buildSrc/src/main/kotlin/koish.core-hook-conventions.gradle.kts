import org.gradle.accessors.dm.LibrariesForLocal

plugins {
    id("koish.core-conventions")
}

val local = the<LibrariesForLocal>()
