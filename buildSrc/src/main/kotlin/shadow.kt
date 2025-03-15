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

    val relocatePrefix = "cc.mewcraft.wakame.shaded."

    fun relocate0(pattern: String) {
        relocate(pattern, relocatePrefix + pattern)
    }

    // fleks
    relocate0("com.github.quillraven.fleks")
    // configurate
    relocate0("org.spongepowered.configurate")
    // commons
    relocate0("xyz.xenondevs.commons")
    // koin
    relocate0("org.koin")
    // creative
    relocate0("team.unnamed.creative")
    // hephaestus
    relocate0("team.unnamed.hephaestus")
    // cloud
    relocate0("org.incendo.cloud")
    // other
    relocate0("io.leangen.geantyref")
    relocate0("xyz.jpenilla.reflectionremapper")
    relocate0("net.fabricmc.mappingio")
}