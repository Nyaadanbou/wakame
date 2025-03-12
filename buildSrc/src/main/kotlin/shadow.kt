import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

fun ShadowJar.configure() {
    archiveClassifier.set("shaded")

    dependencies {
        exclude("about.html")
        exclude("META-INF/maven/**")
        exclude("META-INF/licenses/**")
        exclude("META-INF/versions/**")
        exclude("META-INF/services/**")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
    }

    val pattern = "cc.mewcraft.wakame.shaded."
    // configurate
    relocate("org.spongepowered.configurate", pattern + "org.spongepowered.configurate")
    // commons
    relocate("xyz.xenondevs.commons", pattern + "xyz.xenondevs.commons")
    // koin
    relocate("org.koin", pattern + "org.koin")
    // creative
    relocate("team.unnamed.creative", pattern + "team.unnamed.creative")
    // hephaestus
    relocate("team.unnamed.hephaestus", pattern + "team.unnamed.hephaestus")
    // cloud
    relocate("org.incendo.cloud", pattern + "org.incendo.cloud")
    // other
    relocate("io.leangen.geantyref", pattern + "io.leangen.geantyref")
    relocate("xyz.jpenilla.reflectionremapper", pattern + "xyz.jpenilla.reflectionremapper")
    relocate("net.fabricmc.mappingio", pattern + "net.fabricmc.mappingio")
}