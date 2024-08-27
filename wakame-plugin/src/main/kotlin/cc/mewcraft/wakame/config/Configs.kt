package cc.mewcraft.wakame.config

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.config.Configs.YAML
import cc.mewcraft.wakame.config.Configs.getKoin
import org.jetbrains.annotations.TestOnly
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import org.spongepowered.configurate.loader.AbstractConfigurationLoader
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import kotlin.io.path.Path

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

    @TestOnly
    fun cleanup() {
        YAML.cleanup()
        GSON.cleanup()
    }
}

sealed class BasicConfigs<T : AbstractConfigurationLoader<*>, B : AbstractConfigurationLoader.Builder<B, T>> {
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
    fun build(relPath: String, builder: B.() -> Unit): ConfigProvider {
        val path = Path(relPath)
        return configProviders.getOrPut(path.toString()) { createConfigProvider(relPath, builder) }
    }

    /**
     * Gets a [config provider][ConfigProvider] with the default options.
     */
    operator fun get(relPath: String): ConfigProvider {
        val path = Path(relPath)
        return build(path.toString()) {}
    }

    /**
     * Reloads all the config providers.
     */
    fun reload() {
        configProviders.values.forEach(ConfigProvider::update)
    }

    /**
     * Cleans up the config providers.
     */
    @TestOnly
    fun cleanup() {
        configProviders.clear()
    }

    /**
     * Gets the config file.
     */
    protected fun getConfigFile(path: String): File {
        return getKoin().getOrNull<WakamePlugin>()?.getBundledFile(path) // we're in a server environment
            ?: getKoin().get<File>(named(PLUGIN_DATA_DIR)).resolve(path) // we're in a test environment
    }

    /**
     * Creates a config provider.
     */
    protected abstract fun createConfigProvider(relPath: String, builder: B.() -> Unit): ConfigProvider
}

class YamlConfigs : BasicConfigs<YamlConfigurationLoader, YamlConfigurationLoader.Builder>() {
    override fun createConfigProvider(
        relPath: String,
        builder: YamlConfigurationLoader.Builder.() -> Unit,
    ): ConfigProvider {
        val file = getConfigFile(relPath).toPath()
        return YamlFileConfigProvider(file, relPath, builder)
    }
}

class GsonConfigs : BasicConfigs<GsonConfigurationLoader, GsonConfigurationLoader.Builder>() {
    override fun createConfigProvider(
        relPath: String,
        builder: GsonConfigurationLoader.Builder.() -> Unit,
    ): ConfigProvider {
        val file = getConfigFile(relPath).toPath()
        return GsonFileConfigProvider(file, relPath, builder)
    }
}