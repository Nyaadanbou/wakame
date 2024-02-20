package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.WakamePlugin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.BufferedReader
import java.io.File

fun Scope.getConfigFileAsBufferedReader(path: String): BufferedReader {
    return getOrNull<WakamePlugin>()?.getBundledFile(path)?.bufferedReader() // we're in a server environment
        ?: get<File>(named(PLUGIN_DATA_DIR)).resolve(path).bufferedReader() // we're in a test environment
}

/**
 * Creates a basic builder of configuration loader.
 *
 * @param path the path under the `resources`
 * @param builder the function to modify the type serializer collection
 * @return a builder with basic configuration populated
 */
fun Scope.buildBasicConfigurationLoader(
    path: String,
    builder: TypeSerializerCollection.Builder.() -> Unit = { },
): YamlConfigurationLoader.Builder {
    return buildBasicConfigurationLoader()
        .source { getConfigFileAsBufferedReader(path) }
        .defaultOptions { options -> options.serializers { it.builder() } }
}

/**
 * The same as [buildBasicConfigurationLoader] but you don't need to call
 * `build()` on this.
 */
fun Scope.createBasicConfigurationLoader(
    path: String,
    builder: TypeSerializerCollection.Builder.() -> Unit = { },
): YamlConfigurationLoader {
    return buildBasicConfigurationLoader(path, builder).build()
}