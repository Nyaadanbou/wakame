package cc.mewcraft.wakame.config

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.WakamePlugin
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * The object that manages the configuration providers.
 *
 * This object is **thread-safe**.
 */
object Configs : KoinComponent {
    enum class Type {
        YAML,
        GSON
    }

    private val lock = ReentrantReadWriteLock()

    /**
     * The map of config providers.
     *
     * R - the name of the config provider e.g. "config.yml", "kizami.yml"
     * C - the type of the config provider
     * V - the config provider
     */
    private val configProviders: Table<String, Type, ConfigProvider> = HashBasedTable.create()

    fun reload() {
        lock.read { configProviders.values().forEach(ConfigProvider::update) }
    }

    operator fun get(relPath: String, type: Type = Type.YAML): ConfigProvider {
        return configProviders.safeGetOrPut(relPath, type) { createConfigProvider(relPath, type) }
    }

    private fun createConfigProvider(relPath: String, type: Type): ConfigProvider {
        val file = getConfigFile(relPath).toPath()
        return when (type) {
            Type.YAML -> YamlFileConfigProvider(file, relPath)
            Type.GSON -> GsonFileConfigProvider(file, relPath)
        }
    }

    private fun getConfigFile(path: String): File {
        return getKoin().getOrNull<WakamePlugin>()?.getBundledFile(path) // we're in a server environment
            ?: getKoin().get<File>(named(PLUGIN_DATA_DIR)).resolve(path) // we're in a test environment
    }

    private fun <R, C, V> Table<R, C, V>.safeGetOrPut(row: R, column: C, defaultValue: () -> V): V {
        return lock.read { get(row, column) } ?: lock.write { defaultValue().also { put(row, column, it) } }
    }
}

/**
 * Lazily gets specific value from the **main configuration**, a.k.a. the "config.yml".
 *
 * @param path the path to the config node
 * @param transform the transformation of the config node
 * @return the deserialized value
 */
fun <T> config(vararg path: String, transform: ConfigurationNode.() -> T): Lazy<T> {
    return lazy { Configs["config.yml"].node(*path).get().transform() }
}