package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.WakamePlugin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.BufferedReader
import java.io.File

fun Scope.getConfigFile(path: String): File {
    return getOrNull<WakamePlugin>()?.getBundledFile(path) // we're in a server environment
        ?: get<File>(named(PLUGIN_DATA_DIR)).resolve(path) // we're in a test environment
}

fun Scope.getConfigFileAsBufferedReader(path: String): BufferedReader {
    return getConfigFile(path).bufferedReader()
}

/**
 * Creates a basic builder of yaml configuration loader.
 *
 * @param path the path under the `resources`
 * @param builder the function to modify the type serializer collection
 * @return a builder with basic configuration populated
 */
fun Scope.buildYamlConfigurationLoader(
    path: String,
    builder: TypeSerializerCollection.Builder.() -> Unit = { },
): YamlConfigurationLoader.Builder {
    return buildYamlConfigurationLoader()
        .source { getConfigFileAsBufferedReader(path) }
        .defaultOptions { options -> options.serializers { it.builder() } }
}

/**
 * The same as [buildYamlConfigurationLoader] but you don't need to call
 * `build()` on this.
 */
fun Scope.createYamlConfigurationLoader(
    path: String,
    builder: TypeSerializerCollection.Builder.() -> Unit = { },
): YamlConfigurationLoader {
    return buildYamlConfigurationLoader(path, builder).build()
}

/**
 * Creates a basic builder of gson configuration loader.
 *
 * @param path the path under the `resources`
 * @param builder the function to modify the type serializer collection
 * @return a builder with basic configuration populated
 */
fun Scope.buildGsonConfigurationLoader(
    path: String,
    builder: TypeSerializerCollection.Builder.() -> Unit = { },
): GsonConfigurationLoader.Builder {
    return GsonConfigurationLoader.builder()
        .source { getConfigFileAsBufferedReader(path) }
        .defaultOptions { options -> options.serializers { it.builder() } }
}

/**
 * The same as [buildGsonConfigurationLoader] but you don't need to call
 * `build()` on this.
 */
fun Scope.createGsonConfigurationLoader(
    path: String,
    builder: TypeSerializerCollection.Builder.() -> Unit = { },
): GsonConfigurationLoader {
    return buildGsonConfigurationLoader(path, builder).build()
}
