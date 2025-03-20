package cc.mewcraft.wakame.config

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.feature.Feature
import cc.mewcraft.wakame.lifecycle.initializer.InternalInit
import cc.mewcraft.wakame.lifecycle.initializer.InternalInitStage
import cc.mewcraft.wakame.lifecycle.reloader.InternalReload
import cc.mewcraft.wakame.serialization.configurate.mapperfactory.ObjectMappers
import cc.mewcraft.wakame.serialization.configurate.typeserializer.KOISH_CONFIGURATE_SERIALIZERS
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
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
import kotlin.io.path.exists
import kotlin.io.path.getLastModifiedTime

private val DEFAULT_CONFIG_ID = Identifier.key("koish", "config")

val MAIN_CONFIG: Provider<CommentedConfigurationNode> = Configs[DEFAULT_CONFIG_ID]

/**
 * The object that manages the configuration providers.
 */
@InternalInit(stage = InternalInitStage.PRE_WORLD)
@InternalReload
object Configs {

    private val customSerializers = HashMap<String, TypeSerializerCollection.Builder>()
    private val configProviders = HashMap<Identifier, RootConfigProvider>()

    private const val UNINITIALIZED_LAST_RELOAD = -1L

    /**
     * 最后一次重新加载的时间戳.
     */
    private var lastReload = UNINITIALIZED_LAST_RELOAD

    fun initialize() {
        // 先提取必要的文件到插件数据目录, 否则接下来 reload 会读取到空文件
        ConfigExtractor.extractDefaults()

        // 更新一开始的特殊值为当前时间戳
        lastReload = System.currentTimeMillis()

        // 重新读取已经存在的实例
        configProviders.values.asSequence()
            .filter { it.path.exists() }
            .forEach { it.reload() }
    }

    private fun resolveConfigPath(configId: Identifier): Path {
        val dataFolder = when (configId.namespace()) {
            KOISH_NAMESPACE -> KoishDataPaths.ROOT // -> plugins/<data_folder>
            else -> throw IllegalArgumentException("Only 'koish' namespace is currently supported.")
        }
        return dataFolder.resolve("configs").resolve(configId.value() + ".yml")
    }

    @VisibleForTesting
    internal fun cleanup() {
        configProviders.clear()
    }

    internal fun reload(): List<Identifier> {
        val reloadedConfigs = configProviders
            .asSequence()
            .filter { (_, provider) ->

                // 表示文件之前存在但现在不存在了
                val flag1 = !provider.path.exists() && provider.fileExisted
                // 表示文件存在并且文件的最后修改时间晚于上次重新加载的时间
                val flag2 = provider.path.exists() && (provider.path.getLastModifiedTime().toMillis() > lastReload)

                flag1 || flag2
            }
            .onEach { (_, provider) -> provider.reload() }
            .mapTo(ArrayList()) { (id, _) -> id }

        lastReload = System.currentTimeMillis()

        Bukkit.getOnlinePlayers().forEach(Player::updateInventory)

        return reloadedConfigs
    }

    /**
     * 返回指定配置文件的 [Provider].
     *
     * 传入的 [id] 可以是以下形式, 将转译成具体的文件路径:
     * - `"koish:items"` -> `configs/items.yml`
     * - `"koish:recipe/dirt"` -> `configs/recipe/dirt.yml`
     * - `"koish:enchantment/agility"` -> `configs/enchantment/agility.yml`
     *
     * @param id 配置文件的 id, 必须是 `namespace:path` 的形式. 如果省略 `namespace`, 则默认为 `koish`
     */
    operator fun get(id: String): Provider<CommentedConfigurationNode> {
        return get(Identifiers.of(id))
    }

    /**
     * @param feature 对应的 [Feature] (仅取其命名空间)
     * @param path 相对于 [feature] 文件夹的文件路径
     */
    operator fun get(feature: Feature, path: String): Provider<CommentedConfigurationNode> {
        return get(Identifier.key(feature.namespace, path))
    }

    /**
     * @param id 配置文件的 id, 必须是 `namespace:path` 的形式
     */
    operator fun get(id: Identifier): Provider<CommentedConfigurationNode> {
        return configProviders.getOrPut(id) {
            RootConfigProvider(resolveConfigPath(id), id).also { provider ->
                if (lastReload > UNINITIALIZED_LAST_RELOAD) provider.reload()
            }
        }
    }

    fun getOrNull(id: String): CommentedConfigurationNode? {
        return getOrNull(Identifiers.of(id))
    }

    fun getOrNull(id: Identifier): CommentedConfigurationNode? {
        return configProviders[id]?.takeIf(RootConfigProvider::loaded)?.get()
    }

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("向现有的文件写入内容永远都是一件需要小心的事情")
    fun save(id: String) {
        save(Identifiers.of(id))
    }

    @Deprecated("向现有的文件写入内容永远都是一件需要小心的事情")
    fun save(id: Identifier) {
        val config = getOrNull(id) ?: return
        createLoader(id.namespace(), resolveConfigPath(id)).save(config)
    }

    /**
     * Registers custom [serializers] for configs of [feature].
     */
    fun registerSerializer(feature: Feature, serializers: TypeSerializerCollection) {
        registerSerializer(feature.namespace, serializers)
    }

    /**
     * Registers custom [serializer] for configs of [feature].
     */
    fun <T> registerSerializer(feature: Feature, type: TypeToken<T>, serializer: TypeSerializer<T>) {
        registerSerializer(feature.namespace, type, serializer)
    }

    /**
     * Registers custom [serializer] for configs of [feature].
     */
    inline fun <reified T> registerSerializer(feature: Feature, serializer: TypeSerializer<T>) {
        registerSerializer(feature, typeTokenOf(), serializer)
    }

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

    internal fun createBuilder(namespace: String): YamlConfigurationLoader.Builder {
        return YamlConfigurationLoader.builder()
            .nodeStyle(NodeStyle.BLOCK)
            .indent(2)
            .defaultOptions { opts ->
                opts.serializers { builder ->
                    builder.registerAnnotatedObjects(ObjectMappers.DEFAULT)
                    builder.registerAll(KOISH_CONFIGURATE_SERIALIZERS)
                    customSerializers[namespace]?.build()?.let(builder::registerAll)
                }
            }
    }

    internal fun createLoader(namespace: String, path: Path): YamlConfigurationLoader {
        return createBuilder(namespace).path(path).build()
    }

}