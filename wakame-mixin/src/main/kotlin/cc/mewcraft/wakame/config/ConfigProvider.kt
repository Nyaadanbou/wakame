package cc.mewcraft.wakame.config

import cc.mewcraft.wakame.SharedConstants
import cc.mewcraft.wakame.util.Identifier
import org.jetbrains.annotations.ApiStatus
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ScopedConfigurationNode
import xyz.xenondevs.commons.provider.*
import java.lang.ref.WeakReference
import java.lang.reflect.Type
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import kotlin.io.path.exists
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

/**
 * Gets or creates a [Provider] for a child node under [path].
 */
@JvmName("nodeTyped")
fun <C : ScopedConfigurationNode<C>> Provider<C>.strongNode(vararg path: String): Provider<C> =
    strongMap { node -> node.node(*path) }

/**
 * Gets or creates a [Provider] for a child node under [path].
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 */
@JvmName("weakNodeTyped")
fun <C : ScopedConfigurationNode<C>> Provider<C>.node(vararg path: String): Provider<C> =
    map { node -> node.node(*path) }

/**
 * Gets or creates a [Provider] for a child node under [path].
 */
fun Provider<ConfigurationNode>.strongNode(vararg path: String): Provider<ConfigurationNode> =
    strongMap { node -> node.node(*path) }

/**
 * Gets or creates a [Provider] for a child node under [path].
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 */
fun Provider<ConfigurationNode>.node(vararg path: String): Provider<ConfigurationNode> =
    map { node -> node.node(*path) }

/**
 * Gets an entry [Provider] for a value of type [T] under [path].
 *
 * @throws NoSuchElementException if the entry does not exist
 * @throws IllegalStateException if the entry could not be deserialized to [T]
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.strongEntry(vararg path: String): Provider<T> =
    strongEntry(typeOf<T>().javaType, *path)

/**
 * Gets an entry [Provider] for a value of type [T] under [path].
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 *
 * @throws NoSuchElementException if the entry does not exist
 * @throws IllegalStateException if the entry could not be deserialized to [T]
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.entry(vararg path: String): Provider<T> =
    entry(typeOf<T>().javaType, *path)

/**
 * Gets an entry [Provider] for a value of [type] under [path].
 *
 * @throws NoSuchElementException if the entry does not exist
 * @throws IllegalStateException if the entry could not be deserialized to [type]
 */
fun <T : Any> Provider<ConfigurationNode>.strongEntry(type: KType, vararg path: String): Provider<T> =
    strongEntry(type.javaType, *path)

/**
 * Gets an entry [Provider] for a value of [type] under [path].
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 *
 * @throws NoSuchElementException if the entry does not exist
 * @throws IllegalStateException if the entry could not be deserialized to [type]
 */
fun <T : Any> Provider<ConfigurationNode>.entry(type: KType, vararg path: String): Provider<T> =
    entry(type.javaType, *path)

/**
 * Gets an entry [Provider] for a value of [type] under [path].
 *
 * @throws NoSuchElementException if the entry does not exist
 * @throws IllegalStateException if the entry could not be deserialized to [type]
 */
fun <T : Any> Provider<ConfigurationNode>.strongEntry(type: Type, vararg path: String): Provider<T> =
    strongNode(*path).strongMap { getEntry(it, type, *path) }

/**
 * Gets an entry [Provider] for a value of [type] under [path].
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 *
 * @throws NoSuchElementException if the entry does not exist
 * @throws IllegalStateException if the entry could not be deserialized to [type]
 */
fun <T : Any> Provider<ConfigurationNode>.entry(type: Type, vararg path: String): Provider<T> =
    node(*path).map { getEntry(it, type, *path) }

private fun <T : Any> Provider<ConfigurationNode>.getEntry(node: ConfigurationNode, type: Type, vararg path: String): T {
    if (node.virtual())
        throw NoSuchElementException("Missing config entry '${path.joinToString(" > ")}' in '${fullPath()}'")

    try {
        return node.get(type)!! as T
    } catch (t: Throwable) {
        throw IllegalStateException("Config entry '${fullPath()} > ${path.joinToString(" > ")}' could not be deserialized to $type", t)
    }
}

/**
 * Gets an entry [Provider] for a value of type [T] under the first existing path from [paths].
 *
 * @throws NoSuchElementException if no entry exists
 * @throws IllegalStateException if the entry could not be deserialized to [T]
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.strongEntry(vararg paths: Array<String>): Provider<T> =
    strongEntry(typeOf<T>().javaType, *paths)

/**
 * Gets an entry [Provider] for a value of type [T] under the first existing path from [paths].
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 *
 * @throws NoSuchElementException if no entry exists
 * @throws IllegalStateException if the entry could not be deserialized to [T]
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.entry(vararg paths: Array<String>): Provider<T> =
    entry(typeOf<T>().javaType, *paths)

/**
 * Gets an entry [Provider] for a value of [type] under the first existing path from [paths].
 *
 * @throws NoSuchElementException if no entry exists
 * @throws IllegalStateException if the entry could not be deserialized to [type]
 */
fun <T : Any> Provider<ConfigurationNode>.strongEntry(type: KType, vararg paths: Array<String>): Provider<T> =
    strongEntry(type.javaType, *paths)

/**
 * Gets an entry [Provider] for a value of [type] under the first existing path from [paths].
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 *
 * @throws NoSuchElementException if no entry exists
 * @throws IllegalStateException if the entry could not be deserialized to [type]
 */
fun <T : Any> Provider<ConfigurationNode>.entry(type: KType, vararg paths: Array<String>): Provider<T> =
    entry(type.javaType, *paths)

/**
 * Gets an entry [Provider] for a value of [type] under the first existing path from [paths].
 *
 * @throws NoSuchElementException if no entry exists
 * @throws IllegalStateException if the entry could not be deserialized to [type]
 */
fun <T : Any> Provider<ConfigurationNode>.strongEntry(type: Type, vararg paths: Array<String>): Provider<T> =
    strongCombinedProvider(paths.map { node(*it) }).strongMap { getEntry(it, type, *paths) }

/**
 * Gets an entry [Provider] for a value of [type] under the first existing path from [paths].
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 *
 * @throws NoSuchElementException if no entry exists
 * @throws IllegalStateException if the entry could not be deserialized to [type]
 */
fun <T : Any> Provider<ConfigurationNode>.entry(type: Type, vararg paths: Array<String>): Provider<T> =
    combinedProvider(paths.map { node(*it) }).map { getEntry(it, type, *paths) }

private fun <T : Any> Provider<ConfigurationNode>.getEntry(nodes: List<ConfigurationNode>, type: Type, vararg paths: Array<String>): T {
    var node: ConfigurationNode? = null
    for (possibleNode in nodes) {
        if (!possibleNode.virtual()) {
            node = possibleNode
            break
        }
    }

    if (node == null || node.virtual())
        throw NoSuchElementException("Missing config entry ${paths.joinToString(" or ") { path -> "'${path.joinToString(" > ")}'" }} in ${fullPath()}")

    try {
        return node.get(type)!! as T
    } catch (t: Throwable) {
        throw IllegalStateException("Config entry '${fullPath()} > ${node.path().joinToString(" > ")}' could not be deserialized to $type", t)
    }
}

/**
 * Gets an optional entry [Provider] for a value of type [T] under [path], whose value
 * will be null if the entry does not exist or could not be deserialized to [T].
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.strongOptionalEntry(vararg path: String): Provider<T?> =
    strongOptionalEntry(typeOf<T>().javaType, *path)

/**
 * Gets an optional entry [Provider] for a value of type [T] under [path], whose value
 * will be null if the entry does not exist or could not be deserialized to [T].
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.optionalEntry(vararg path: String): Provider<T?> =
    optionalEntry(typeOf<T>().javaType, *path)

/**
 * Gets an optional entry [Provider] for a value of [type] under [path], whose value
 * will be null if the entry does not exist or could not be deserialized to [type].
 */
fun <T : Any> Provider<ConfigurationNode>.strongOptionalEntry(type: KType, vararg path: String): Provider<T?> =
    strongOptionalEntry(type.javaType, *path)

/**
 * Gets an optional entry [Provider] for a value of [type] under [path], whose value
 * will be null if the entry does not exist or could not be deserialized to [type].
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 */
fun <T : Any> Provider<ConfigurationNode>.optionalEntry(type: KType, vararg path: String): Provider<T?> =
    optionalEntry(type.javaType, *path)

/**
 * Gets an optional entry [Provider] for a value of [type] under [path], whose value
 * will be null if the entry does not exist or could not be deserialized to [type].
 */
fun <T : Any> Provider<ConfigurationNode>.strongOptionalEntry(type: Type, vararg path: String): Provider<T?> =
    strongNode(*path).strongMap { if (!it.virtual()) it.get(type) as? T else null }

/**
 * Gets an optional entry [Provider] for a value of [type] under [path], whose value
 * will be null if the entry does not exist or could not be deserialized to [type].
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 */
fun <T : Any> Provider<ConfigurationNode>.optionalEntry(type: Type, vararg path: String): Provider<T?> =
    node(*path).map { if (!it.virtual()) it.get(type) as? T else null }

/**
 * Gets an optional entry [Provider] for a value of type [T] under the first existing path from [paths],
 * whose value will be null if no entry exists or could not be deserialized to [T].
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.strongOptionalEntry(vararg paths: Array<String>): Provider<T?> =
    strongOptionalEntry(typeOf<T>().javaType, *paths)

/**
 * Gets an optional entry [Provider] for a value of type [T] under the first existing path from [paths],
 * whose value will be null if no entry exists or could not be deserialized to [T].
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.optionalEntry(vararg paths: Array<String>): Provider<T?> =
    optionalEntry(typeOf<T>().javaType, *paths)

/**
 * Gets an optional entry [Provider] for a value of [type] under the first existing path from [paths],
 * whose value will be null if no entry exists or could not be deserialized to [type].
 */
fun <T : Any> Provider<ConfigurationNode>.strongOptionalEntry(type: KType, vararg paths: Array<String>): Provider<T?> =
    strongOptionalEntry(type.javaType, *paths)

/**
 * Gets an optional entry [Provider] for a value of [type] under the first existing path from [paths],
 * whose value will be null if no entry exists or could not be deserialized to [type].
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 */
fun <T : Any> Provider<ConfigurationNode>.optionalEntry(type: KType, vararg paths: Array<String>): Provider<T?> =
    optionalEntry(type.javaType, *paths)

/**
 * Gets an optional entry [Provider] for a value of [type] under the first existing path from [paths],
 * whose value will be null if no entry exists or could not be deserialized to [type].
 */
fun <T : Any> Provider<ConfigurationNode>.strongOptionalEntry(type: Type, vararg paths: Array<String>): Provider<T?> =
    strongCombinedProvider(paths.map { node(*it) }).strongMap { getOptionalEntry(it, type) }

/**
 * Gets an optional entry [Provider] for a value of [type] under the first existing path from [paths],
 * whose value will be null if no entry exists or could not be deserialized to [type].
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 */
fun <T : Any> Provider<ConfigurationNode>.optionalEntry(type: Type, vararg paths: Array<String>): Provider<T?> =
    combinedProvider(paths.map { node(*it) }).map { getOptionalEntry(it, type) }

private fun <T : Any> Provider<ConfigurationNode>.getOptionalEntry(nodes: List<ConfigurationNode>, type: Type): T? {
    var node: ConfigurationNode? = null
    for (possibleNode in nodes) {
        if (!possibleNode.virtual()) {
            node = possibleNode
            break
        }
    }

    return node?.get(type) as? T
}

/**
 * Gets an entry [Provider] for a value of type [T] under [path], using [default] as fallback, or requiring config presence if null.
 *
 * @throws NoSuchElementException if the entry does not exist and [default] is null
 * @throws IllegalStateException if the entry could not be deserialized to [T]
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.strongEntryOrElse(default: T?, vararg path: String): Provider<T> =
    if (default != null) strongOptionalEntry<T>(*path).strongOrElse(default) else strongEntry<T>(*path)

/**
 * Gets an entry [Provider] for a value of type [T] under [path], using [default] as fallback, or requiring config presence if null.
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 *
 * @throws NoSuchElementException if the entry does not exist and [default] is null
 * @throws IllegalStateException if the entry could not be deserialized to [T]
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.entryOrElse(default: T?, vararg path: String): Provider<T> =
    if (default != null) optionalEntry<T>(*path).orElse(default) else entry<T>(*path)

/**
 * Gets an entry [Provider] for a value of type [T] under the first existing path from [paths],
 * using [default] as fallback, or requiring config presence if null.
 *
 * @throws NoSuchElementException if no entry exists and [default] is null
 * @throws IllegalStateException if the entry could not be deserialized to [T]
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.strongEntryOrElse(default: T?, vararg paths: Array<String>): Provider<T> =
    if (default != null) strongOptionalEntry<T>(*paths).strongOrElse(default) else strongEntry<T>(*paths)

/**
 * Gets an entry [Provider] for a value of type [T] under the first existing path from [paths],
 * using [default] as fallback, or requiring config presence if null.
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 *
 * @throws NoSuchElementException if no entry exists and [default] is null
 * @throws IllegalStateException if the entry could not be deserialized to [T]
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.entryOrElse(default: T?, vararg paths: Array<String>): Provider<T> =
    if (default != null) optionalEntry<T>(*paths).orElse(default) else entry<T>(*paths)

/**
 * Gets an entry [Provider] for a value of type [T] under [path], using [default] as fallback, or requiring config presence if null.
 *
 * @throws NoSuchElementException if the entry does not exist and [default] is null
 * @throws IllegalStateException if the entry could not be deserialized to [T]
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.strongEntryOrElse(default: Provider<T>?, vararg path: String): Provider<T> =
    if (default != null) strongOptionalEntry<T>(*path).strongOrElse(default) else strongEntry<T>(*path)

/**
 * Gets an entry [Provider] for a value of type [T] under [path], using [default] as fallback, or requiring config presence if null.
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 *
 * @throws NoSuchElementException if the entry does not exist and [default] is null
 * @throws IllegalStateException if the entry could not be deserialized to [T]
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.entryOrElse(default: Provider<T>?, vararg path: String): Provider<T> =
    if (default != null) optionalEntry<T>(*path).orElse(default) else entry<T>(*path)

/**
 * Gets an entry [Provider] for a value of type [T] under the first existing path from [paths],
 * using [default] as fallback, or requiring config presence if null.
 *
 * @throws NoSuchElementException if no entry exists and [default] is null
 * @throws IllegalStateException if the entry could not be deserialized to [T]
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.strongEntryOrElse(default: Provider<T>?, vararg paths: Array<String>): Provider<T> =
    if (default != null) strongOptionalEntry<T>(*paths).strongOrElse(default) else strongEntry<T>(*paths)

/**
 * Gets an entry [Provider] for a value of type [T] under the first existing path from [paths],
 * using [default] as fallback, or requiring config presence if null.
 *
 * The returned provider will only be stored in a [WeakReference] in the parent provider.
 *
 * @throws NoSuchElementException if no entry exists and [default] is null
 * @throws IllegalStateException if the entry could not be deserialized to [T]
 */
inline fun <reified T : Any> Provider<ConfigurationNode>.entryOrElse(default: Provider<T>?, vararg paths: Array<String>): Provider<T> =
    if (default != null) optionalEntry<T>(*paths).orElse(default) else entry<T>(*paths)

private fun Provider<ConfigurationNode>.fullPath(): String {
    val filePath = findFilePath()
    val path = get().path()

    val builder = StringBuilder()
    if (path.size() > 0) {
        if (filePath != null) {
            builder.append(filePath)
            builder.append(" > ")
        }
        builder.append(path.joinToString(" > "))
    } else if (filePath != null) {
        builder.append(filePath)
    }

    return builder.toString()
}

@OptIn(UnstableProviderApi::class)
private fun Provider<ConfigurationNode>.findFilePath(): String? {
    this as AbstractProvider<ConfigurationNode>

    val queue = ArrayDeque<AbstractProvider<*>>()
    queue.add(this)

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        if (current is RootConfigProvider) {
            return current.configId.toString()
        }
        queue.addAll(current.parents)
    }

    return null
}

@ApiStatus.Internal
@OptIn(UnstableProviderApi::class)
class RootConfigProvider(
    val path: Path,
    val configId: Identifier,
) : AbstractProvider<CommentedConfigurationNode>(ReentrantLock()) {

    @Volatile
    var loaded: Boolean = false
        private set

    @Volatile
    var fileExisted: Boolean = false
        private set

    init {
        subscribe {
            loaded = true
            fileExisted = path.exists()
        }
    }

    fun reload() {
        set(loadNode())
    }

    override fun pull(): CommentedConfigurationNode {
        if (SharedConstants.isRunningInIde) {
            // 如果是测试环境, 则直接从文件加载, 无需等待 (re)load
            return loadNode()
        }

        // empty placeholder that is replaced by the actual node when the config is (re)loaded
        return ConfigAccess.INSTANCE.createBuilder(configId.namespace()).build().createNode()
    }

    private fun loadNode(): CommentedConfigurationNode {
        return ConfigAccess.INSTANCE.createLoader(configId.namespace(), path).load()
    }

}