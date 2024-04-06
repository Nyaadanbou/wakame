package cc.mewcraft.wakame.config

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.WakamePlugin
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import java.io.File

object Configs : KoinComponent {
    /**
     * The map of config providers.
     *
     * K - the name of the config provider e.g. "config.yml", "kizami.yml"
     * V - the config provider
     */
    private val configProviders: MutableMap<String, ConfigProvider> = hashMapOf()

    fun onReload() {
        configProviders.values.forEach(ConfigProvider::update)
    }

    operator fun get(relPath: String): ConfigProvider {
        return configProviders.getOrPut(relPath) { createConfigProvider(relPath) }
    }

    private fun createConfigProvider(relPath: String): ConfigProvider {
        val file = getConfigFile(relPath).toPath()
        return FileConfigProvider(file, relPath)
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
fun <T> config(vararg path: String, transform: ConfigurationNode.() -> T): Lazy<T> {
    return lazy { Configs["config.yml"].node(*path).get().transform() }
}