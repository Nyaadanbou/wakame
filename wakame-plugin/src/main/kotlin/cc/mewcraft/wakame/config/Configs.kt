package cc.mewcraft.wakame.config

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.WakamePlugin
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import java.io.File

/**
 * The object that manages the configuration providers.
 */
object Configs : KoinComponent {
    enum class Type {
        YAML,
        GSON
    }

    /**
     * The map of config providers.
     *
     * R - the name of the config provider e.g. "config.yml", "kizami.yml"
     * C - the type of the config provider
     * V - the config provider
     */
    private val configProviders: Table<String, Type, ConfigProvider> = HashBasedTable.create()

    fun reload() {
        configProviders.values().forEach(ConfigProvider::update)
    }

    operator fun get(
        relPath: String,
        type: Type = Type.YAML,
        options: ConfigurationOptions.() -> ConfigurationOptions = { ConfigurationOptions.defaults() }
    ): ConfigProvider {
        return configProviders.getOrPut(relPath, type) { createConfigProvider(relPath, type, options) }
    }

    private fun <R, C, V> Table<R, C, V>.getOrPut(row: R, column: C, defaultValue: () -> V): V {
        return get(row, column) ?: defaultValue().also { put(row, column, it) }
    }

    private fun createConfigProvider(
        relPath: String,
        type: Type,
        options: ConfigurationOptions.() -> ConfigurationOptions
    ): ConfigProvider {
        val file = getConfigFile(relPath).toPath()
        return when (type) {
            Type.YAML -> YamlFileConfigProvider(file, relPath, options)
            Type.GSON -> GsonFileConfigProvider(file, relPath, options)
        }
    }

    private fun getConfigFile(path: String): File {
        return getKoin().getOrNull<WakamePlugin>()?.getBundledFile(path) // we're in a server environment
            ?: getKoin().get<File>(named(PLUGIN_DATA_DIR)).resolve(path) // we're in a test environment
    }
}

/**
 * Lazily gets specific value from the **main configuration**, a.k.a. the "config.yml".
 *
 * @param path the path to the config node
 * @param transform the transformation of the config node
 * @return the deserialized value
 */
fun <T> config(vararg path: String, transform: ConfigurationNode.() -> T): Provider<T> {
    return Configs["config.yml"].node(*path).map(transform)
}