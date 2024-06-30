package cc.mewcraft.wakame.config

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.config.Configs.YAML
import cc.mewcraft.wakame.config.Configs.getKoin
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
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

sealed class BasicConfigs<T: AbstractConfigurationLoader<*>, B : AbstractConfigurationLoader.Builder<B, T>> {
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
        builder: B.() -> Unit
    ): ConfigProvider {
        return configProviders.getOrPut(relPath) { createConfigProvider(relPath, builder) }
    }

    /**
     * Gets a [config provider][ConfigProvider] with the default options.
     */
    operator fun get(relPath: String): ConfigProvider {
        return configProviders.getOrPut(relPath) { createConfigProvider(relPath, {}) }
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
        builder: B.() -> Unit
    ): ConfigProvider
}

class YamlConfigs : BasicConfigs<YamlConfigurationLoader, YamlConfigurationLoader.Builder>() {
    override fun createConfigProvider(
        relPath: String,
        builder: YamlConfigurationLoader.Builder.() -> Unit
    ): ConfigProvider {
        val file = getConfigFile(relPath).toPath()
        return YamlFileConfigProvider(file, relPath, builder)
    }
}

class GsonConfigs : BasicConfigs<GsonConfigurationLoader, GsonConfigurationLoader.Builder>() {
    override fun createConfigProvider(
        relPath: String,
        builder: GsonConfigurationLoader.Builder.() -> Unit
    ): ConfigProvider {
        val file = getConfigFile(relPath).toPath()
        return GsonFileConfigProvider(file, relPath, builder)
    }
}