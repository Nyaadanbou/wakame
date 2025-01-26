package cc.mewcraft.wakame.config

import cc.mewcraft.wakame.InjectionQualifier
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.KOISH_JAR
import cc.mewcraft.wakame.feature.Feature
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InternalInit
import cc.mewcraft.wakame.initializer2.InternalInitStage
import cc.mewcraft.wakame.serialization.configurate.typeserializer.KOISH_CONFIGURATE_SERIALIZERS
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import cc.mewcraft.wakame.util.data.useZip
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.annotations.VisibleForTesting
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import xyz.xenondevs.commons.provider.Provider
import java.nio.file.Path
import kotlin.io.path.*

private val DEFAULT_CONFIG_ID = Identifier.key("koish", "configs")
private const val DEFAULT_CONFIG_PATH = "configs/config.yml" // relative to Plugin#dataFolder
val MAIN_CONFIG: Provider<CommentedConfigurationNode> = Configs[DEFAULT_CONFIG_ID]

/**
 * The object that manages the configuration providers.
 */
@InternalInit(
    stage = InternalInitStage.PRE_WORLD,
)
object Configs {

    private val customSerializers = HashMap<String, TypeSerializerCollection.Builder>()

    private val configExtractor = ConfigExtractor(PermanentStorage.storedValue("stored_configs", ::HashMap))
    private val configProviders = HashMap<Identifier, RootConfigProvider>()

    private var lastReload = -1L

    internal fun extractDefaultConfig() {
        KOISH_JAR.useZip { zip ->
            val from = zip.resolve(DEFAULT_CONFIG_PATH)
            val to = Injector.get<Path>(InjectionQualifier.DATA_FOLDER).resolve(DEFAULT_CONFIG_PATH)
            extractConfig(from, to, DEFAULT_CONFIG_ID)
        }
    }

    @InitFun
    private fun extractAllConfigs() {
        extractConfigs("koish", KOISH_JAR, Injector.get<Path>(InjectionQualifier.DATA_FOLDER))
        // TODO: 提取 Feature 的配置文件

        lastReload = System.currentTimeMillis()
        configProviders.values.asSequence()
            .filter { it.path.exists() }
            .forEach { it.reload() } // 调用 RootConfigProvider#reload 方法
    }

    private fun extractConfigs(namespace: String, zipFile: Path, dataFolder: Path) {
        zipFile.useZip { zip ->
            val configsDir = zip.resolve("configs/")
            configsDir.walk()
                .filter { !it.isDirectory() && it.extension.equals("yml", true) }
                .forEach { config ->
                    val relPath = config.relativeTo(configsDir).invariantSeparatorsPathString
                    val configId = Identifier.key(namespace, relPath.substringBeforeLast('.'))
                    extractConfig(config, dataFolder.resolve("configs").resolve(relPath), configId)
                }
        }
    }

    private fun extractConfig(from: Path, to: Path, configId: Identifier) {
        configExtractor.extract(configId, from, to)
        val provider = configProviders.getOrPut(configId) { RootConfigProvider(to, configId) }
        provider.reload()
    }

    private fun resolveConfigPath(configId: Identifier): Path {
        val dataFolder = when (configId.namespace()) {
            KOISH_NAMESPACE -> Injector.get<Path>(InjectionQualifier.DATA_FOLDER) // -> plugins/Koish
            else -> throw IllegalArgumentException("Only 'koish' namespace is currently supported.")
        }
        return dataFolder.resolve("configs").resolve(configId.value() + ".yml")
    }

    @VisibleForTesting
    internal fun cleanup() {
        configProviders.clear()
    }

    internal fun reload(): List<Identifier> {
        val reloadedConfigs = configProviders.asSequence()
            .filter { (_, provider) ->
                // 表示文件之前存在但现在不存在了
                (!provider.path.exists() && provider.fileExisted) ||
                    // 表示文件存在，并且文件的最后修改时间晚于上次重新加载的时间
                    (provider.path.exists() && (provider.path.getLastModifiedTime().toMillis() > lastReload))
            } // 只重新加载动过的文件
            .onEach { (_, provider) -> provider.reload() } // 调用 RootConfigProvider#reload 方法
            .mapTo(ArrayList()) { (id, _) -> id }

        lastReload = System.currentTimeMillis()

        Bukkit.getOnlinePlayers().forEach(Player::updateInventory)

        return reloadedConfigs
    }

    /**
     * @param id 配置文件的 id, 必须是 `namespace:path` 的形式. 如果省略 `namespace`, 则默认为 `koish`
     */
    operator fun get(id: String): Provider<CommentedConfigurationNode> =
        get(Identifiers.of(id))

    /**
     * @param feature 对应的 Feature (仅取其命名空间)
     * @param path 相对于 [feature] 文件夹的文件路径
     */
    operator fun get(feature: Feature, path: String): Provider<CommentedConfigurationNode> =
        get(Identifier.key(feature.namespace, path))

    /**
     * @param id 配置文件的 id, 必须是 `namespace:path` 的形式
     */
    operator fun get(id: Identifier): Provider<CommentedConfigurationNode> =
        configProviders.getOrPut(id) { RootConfigProvider(resolveConfigPath(id), id).also { if (lastReload > -1) it.reload() } }

    fun getOrNull(id: String): CommentedConfigurationNode? =
        getOrNull(Identifiers.of(id))

    fun getOrNull(id: Identifier): CommentedConfigurationNode? =
        configProviders[id]?.takeIf(RootConfigProvider::loaded)?.get()

    fun save(id: String): Unit =
        save(Identifiers.of(id))

    fun save(id: Identifier) {
        val config = getOrNull(id) ?: return
        createLoader(id.namespace(), resolveConfigPath(id)).save(config)
    }

    /**
     * Registers custom [serializers] for configs of [feature].
     */
    fun registerSerializer(feature: Feature, serializers: TypeSerializerCollection) =
        registerSerializer(feature.namespace, serializers)

    /**
     * Registers custom [serializer] for configs of [feature].
     */
    fun <T> registerSerializer(feature: Feature, type: TypeToken<T>, serializer: TypeSerializer<T>) =
        registerSerializer(feature.namespace, type, serializer)

    /**
     * Registers custom [serializer] for configs of [feature].
     */
    inline fun <reified T> registerSerializer(feature: Feature, serializer: TypeSerializer<T>) =
        registerSerializer(feature, typeTokenOf(), serializer)

    /**
     * Registers custom [serializers] for configs of [namespace].
     */
    fun registerSerializer(namespace: String, serializers: TypeSerializerCollection) {
        customSerializers.getOrPut(namespace, TypeSerializerCollection::builder).registerAll(serializers)
    }

    /**
     * Registers custom [serializer] for configs of [namespace].
     */
    fun <T> registerSerializer(namespace: String, type: TypeToken<T>, serializer: TypeSerializer<T>) {
        customSerializers.getOrPut(namespace, TypeSerializerCollection::builder).register(type, serializer)
    }

    /**
     * Registers custom [serializer] for configs of [namespace].
     */
    inline fun <reified T> registerSerializer(namespace: String, serializer: TypeSerializer<T>) {
        registerSerializer(namespace, typeTokenOf(), serializer)
    }

    internal fun createBuilder(namespace: String): YamlConfigurationLoader.Builder =
        YamlConfigurationLoader.builder()
            .nodeStyle(NodeStyle.BLOCK)
            .indent(2)
            .defaultOptions { opts ->
                opts.serializers { builder ->
                    builder.registerAll(KOISH_CONFIGURATE_SERIALIZERS)
                    customSerializers[namespace]?.build()?.let { builder.registerAll(it) }
                }
            }

    internal fun createLoader(namespace: String, path: Path): YamlConfigurationLoader =
        createBuilder(namespace).path(path).build()

}