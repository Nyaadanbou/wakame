package cc.mewcraft.wakame.config

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.config.Configs.YAML
import cc.mewcraft.wakame.config.Configs.getKoin
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationOptions
import java.io.File

val MAIN_CONFIG: ConfigProvider by lazy { YAML["config.yml"] }

/**
 * The object that manages the configuration providers.
 */
object Configs : KoinComponent {
    val YAML: YamlConfigs = YamlConfigs()
    val GSON: GsonConfigs = GsonConfigs()

    fun reload() {
        YAML.reload()
        GSON.reload()
    }
}

sealed class BasicConfigs {
    /**
     * The map of config providers.
     *
     * K - the name of the config provider e.g. "config.yml", "kizami.yml"
     * V - the config provider
     */
    private val configProviders: MutableMap<String, ConfigProvider> = HashMap()

    /**
     * Builds a [config provider][ConfigProvider] with the specified options.
     */
    fun build(
        relPath: String,
        options: ConfigurationOptions.() -> ConfigurationOptions,
    ): ConfigProvider {
        return configProviders.getOrPut(relPath) { createConfigProvider(relPath, options) }
    }

    /**
     * Gets a [config provider][ConfigProvider] with the default options.
     */
    operator fun get(relPath: String): ConfigProvider {
        return configProviders.getOrPut(relPath) { createConfigProvider(relPath) { this } }
    }

    fun reload() {
        configProviders.values.forEach(ConfigProvider::update)
    }

    protected fun getConfigFile(path: String): File {
        return getKoin().getOrNull<WakamePlugin>()?.getBundledFile(path) // we're in a server environment
            ?: getKoin().get<File>(named(PLUGIN_DATA_DIR)).resolve(path) // we're in a test environment
    }

    protected abstract fun createConfigProvider(
        relPath: String,
        options: ConfigurationOptions.() -> ConfigurationOptions
    ): ConfigProvider
}

class YamlConfigs : BasicConfigs() {
    override fun createConfigProvider(
        relPath: String,
        options: ConfigurationOptions.() -> ConfigurationOptions
    ): ConfigProvider {
        val file = getConfigFile(relPath).toPath()
        return YamlFileConfigProvider(file, relPath, options)
    }
}

class GsonConfigs : BasicConfigs() {
    override fun createConfigProvider(
        relPath: String,
        options: ConfigurationOptions.() -> ConfigurationOptions
    ): ConfigProvider {
        val file = getConfigFile(relPath).toPath()
        return GsonFileConfigProvider(file, relPath, options)
    }
}