@file:Suppress("UNCHECKED_CAST")

package cc.mewcraft.wakame.config

import cc.mewcraft.wakame.util.withDefaults
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import xyz.xenondevs.commons.provider.AbstractProvider
import xyz.xenondevs.commons.provider.Provider
import xyz.xenondevs.commons.provider.immutable.combinedProvider
import xyz.xenondevs.commons.provider.immutable.map
import java.lang.reflect.Type
import java.nio.file.Path
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

/**
 * A [ConfigProvider] with a YAML file being its data source.
 *
 * @param path the file path to the YAML
 * @param relPath the relative file path
 * @param builder the configuration loader builder
 */
class YamlFileConfigProvider internal constructor(
    private val path: Path,
    relPath: String,
    private val builder: YamlConfigurationLoader.Builder.() -> Unit,
) : ConfigProvider(null, "", relPath) {

    override fun loadValue(): ConfigurationNode {
        return YamlConfigurationLoader.builder()
            .source { path.toFile().bufferedReader() }
            .withDefaults()
            .apply(builder)
            .build()
            .load()
    }
}

/**
 * A [ConfigProvider] with a JSON file being its data source.
 *
 * @param path the file path to the JSON
 * @param relPath the relative file path
 * @param builder the configuration loader builder
 */
class GsonFileConfigProvider internal constructor(
    private val path: Path,
    relPath: String,
    private val builder: GsonConfigurationLoader.Builder.() -> Unit,
) : ConfigProvider(null, "", relPath) {

    override fun loadValue(): ConfigurationNode {
        return GsonConfigurationLoader.builder()
            .source { path.toFile().bufferedReader() }
            .apply(builder)
            .build()
            .load()
    }
}

open class ConfigProvider(
    private val parent: ConfigProvider?,
    private val node: String,
    private val relPath: String,
) : AbstractProvider<ConfigurationNode>() {

    /**
     * Cache for child node providers.
     */
    private val nodes = HashMap<String, ConfigProvider>()

    /**
     * Loads the [ConfigurationNode] of this [ConfigProvider].
     */
    override fun loadValue(): ConfigurationNode {
        return parent!!.get().node(node)
    }

    /**
     * Gets or creates a [ConfigProvider] for a child node under [path].
     */
    fun node(vararg path: String): ConfigProvider {
        if (path.isEmpty())
            return this

        val name = path[0]
        return nodes.getOrPut(name) {
            val node = ConfigProvider(this, name, "$relPath > $name")
            addChild(node)
            node
        }.node(*path.copyOfRange(1, path.size))
    }

    /**
     * Gets an entry [Provider] for a value of type [T] under [path].
     *
     * @throws NoSuchElementException if the entry does not exist
     * @throws IllegalStateException if the entry could not be deserialized to [T]
     */
    inline fun <reified T : Any> entry(vararg path: String): Provider<T> =
        entry(typeOf<T>().javaType, *path)

    /**
     * Gets an entry [Provider] for a value of [type] under [path].
     *
     * @throws NoSuchElementException if the entry does not exist
     * @throws IllegalStateException if the entry could not be deserialized to [type]
     */
    fun <T : Any> entry(type: KType, vararg path: String): Provider<T> =
        entry(type.javaType, *path)

    /**
     * Gets an entry [Provider] for a value of [type] under [path].
     *
     * @throws NoSuchElementException if the entry does not exist
     * @throws IllegalStateException if the entry could not be deserialized to [type]
     */
    fun <T : Any> entry(type: Type, vararg path: String): Provider<T> =
        node(*path).map { node ->
            if (node.virtual())
                throw NoSuchElementException("Missing config entry '${path.joinToString(" > ")}' in $relPath")

            try {
                return@map node.get(type)!! as T
            } catch (t: Throwable) {
                throw IllegalStateException("Config entry '${node.path().joinToString(" > ")}' in $relPath could not be deserialized to $type", t)
            }
        }

    /**
     * Gets an entry [Provider] for a value of type [T] under the first existing path from [paths].
     *
     * @throws NoSuchElementException if no entry exists
     * @throws IllegalStateException if the entry could not be deserialized to [T]
     */
    inline fun <reified T : Any> entry(vararg paths: Array<String>): Provider<T> =
        entry(typeOf<T>().javaType, *paths)

    /**
     * Gets an entry [Provider] for a value of [type] under the first existing path from [paths].
     *
     * @throws NoSuchElementException if no entry exists
     * @throws IllegalStateException if the entry could not be deserialized to [type]
     */
    fun <T : Any> entry(type: KType, vararg paths: Array<String>): Provider<T> =
        entry(type.javaType, *paths)

    /**
     * Gets an entry [Provider] for a value of [type] under the first existing path from [paths].
     *
     * @throws NoSuchElementException if no entry exists
     * @throws IllegalStateException if the entry could not be deserialized to [type]
     */
    fun <T : Any> entry(type: Type, vararg paths: Array<String>): Provider<T> =
        combinedProvider(paths.map { node(*it) }).map { nodes ->
            var node: ConfigurationNode? = null
            for (possibleNode in nodes) {
                if (!possibleNode.virtual()) {
                    node = possibleNode
                    break
                }
            }

            if (node == null || node.virtual())
                throw NoSuchElementException("Missing config entry ${paths.joinToString(" or ") { path -> "'${path.joinToString(" > ")}'" }} in $relPath")

            try {
                return@map node.get(type)!! as T
            } catch (t: Throwable) {
                throw IllegalStateException("Config entry '$relPath ${node.path().joinToString(" > ")}' could not be deserialized to $type", t)
            }
        }

    /**
     * Gets an optional entry [Provider] for a value of type [T] under [path], whose value
     * will be null if the entry does not exist or could not be deserialized to [T].
     */
    inline fun <reified T : Any> optionalEntry(vararg path: String): Provider<T?> =
        optionalEntry(typeOf<T>().javaType, *path)

    /**
     * Gets an optional entry [Provider] for a value of [type] under [path], whose value
     * will be null if the entry does not exist or could not be deserialized to [type].
     */
    fun <T : Any> optionalEntry(type: KType, vararg path: String): Provider<T?> =
        optionalEntry(type.javaType, *path)

    /**
     * Gets an optional entry [Provider] for a value of [type] under [path], whose value
     * will be null if the entry does not exist or could not be deserialized to [type].
     */
    fun <T : Any> optionalEntry(type: Type, vararg path: String): Provider<T?> =
        node(*path).map { if (!it.virtual()) it.get(type) as? T else null }

    /**
     * Gets an optional entry [Provider] for a value of type [T] under the first existing path from [paths],
     * whose value will be null if no entry exists or could not be deserialized to [T].
     */
    inline fun <reified T : Any> optionalEntry(vararg paths: Array<String>): Provider<T?> =
        optionalEntry(typeOf<T>().javaType, *paths)

    /**
     * Gets an optional entry [Provider] for a value of [type] under the first existing path from [paths],
     * whose value will be null if no entry exists or could not be deserialized to [type].
     */
    fun <T : Any> optionalEntry(type: KType, vararg paths: Array<String>): Provider<T?> =
        optionalEntry(type.javaType, *paths)

    /**
     * Gets an optional entry [Provider] for a value of [type] under the first existing path from [paths],
     * whose value will be null if no entry exists or could not be deserialized to [type].
     */
    fun <T : Any> optionalEntry(type: Type, vararg paths: Array<String>): Provider<T?> =
        combinedProvider(paths.map { node(*it) }).map { nodes ->
            var node: ConfigurationNode? = null
            for (possibleNode in nodes) {
                if (!possibleNode.virtual()) {
                    node = possibleNode
                    break
                }
            }

            node?.get(type) as? T
        }
}
