package cc.mewcraft.lazyconfig.access

import cc.mewcraft.lazyconfig.configurate.STANDARD_SERIALIZERS
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import xyz.xenondevs.commons.provider.Provider
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.getLastModifiedTime


/**
 * 配置系统的抽象实现基类。
 *
 * 子类应该实现配置文件的路径解析和加载逻辑。
 */
abstract class ConfigsBase : ConfigAccess {

    protected companion object {
        const val UNINITIALIZED_LAST_RELOAD = -1L
    }

    /**
     * 映射: 配置文件 [Key.namespace] -> [TypeSerializerCollection].
     */
    protected val customSerializers = HashMap<String, TypeSerializerCollection.Builder>()

    /**
     * 映射: 配置文件 [Key] -> [RootConfigProvider].
     */
    protected val configProviders = HashMap<Key, RootConfigProvider>()

    /**
     * 最后一次重新加载的时间戳.
     */
    protected var lastReloadTimestamp = UNINITIALIZED_LAST_RELOAD

    fun initialize() {
        // 注册 ConfigAccess 实例
        ConfigAccess.setImplementation(this)

        // 先提取必要的文件到插件数据目录, 否则接下来 reload 会读取到空文件
        this.extractDefaultFiles()

        // 更新一开始的特殊值为当前时间戳
        this.lastReloadTimestamp = System.currentTimeMillis()

        // 重新加载已经存在的实例
        this.configProviders.values
            .asSequence()
            .filter { it.path.exists() }
            .forEach { it.reload() }
    }

    /**
     * 获取默认命名空间.
     */
    protected abstract fun defaultNamespace(): String

    /**
     * 提取默认配置文件到数据目录.
     */
    protected abstract fun extractDefaultFiles()

    /**
     * 获取给定配置 [configId] 的配置文件路径.
     *
     * ### 实现要求
     *
     * 子类必须实现此方法以提供具体的路径解析逻辑.
     *
     * @param configId 配置文件的 [Key]
     * @return 配置文件的路径
     */
    protected abstract fun resolvePath(configId: Key): Path

    /**
     * 构建给定命名空间的 [TypeSerializerCollection].
     *
     * ### 实现要求
     *
     * 子类可以重写此方法以添加命名空间特定的 [TypeSerializerCollection].
     *
     * 为了降低重复代码, 可以采用以下模式:
     *
     * ```kotlin
     * val children = super.buildSerials(namespace)
     *     .childBuilder()
     *     // 进一步修改 children ...
     *     .build()
     * return children
     * ```
     */
    protected open fun buildSerials(namespace: String): TypeSerializerCollection {
        val builder = TypeSerializerCollection.builder()
        this.customSerializers[namespace]?.build()?.let(builder::registerAll)
        builder.registerAll(STANDARD_SERIALIZERS)
        val serials = builder.build()
        return serials
    }

    /**
     * 重新加载后的回调函数.
     *
     * @param keys 重新加载的配置文件 [Key] 列表
     */
    protected open fun afterReload(keys: List<Key>) = Unit

    //

    override fun reload(): List<Key> {
        val reloadedConfigKeys = this.configProviders
            .asSequence()
            .filter { (_, provider) ->
                // 表示文件之前存在但现在不存在了
                val flag1 = !provider.path.exists() && provider.fileExisted
                // 表示文件存在并且文件的最后修改时间晚于上次重新加载的时间
                val flag2 = provider.path.exists() && (provider.path.getLastModifiedTime().toMillis() > lastReloadTimestamp)

                flag1 || flag2
            }
            .onEach { (_, provider) -> provider.reload() }
            .mapTo(ArrayList()) { (id, _) -> id }

        // 更新最后重新加载的时间戳
        this.lastReloadTimestamp = System.currentTimeMillis()

        // 调用回调函数, 让上层处理重新加载后的逻辑
        this.afterReload(reloadedConfigKeys)

        return reloadedConfigKeys
    }

    override operator fun get(id: String): Provider<CommentedConfigurationNode> {
        val key = if (':' in id) Key.key(id) else Key.key(defaultNamespace(), id)
        return get(key)
    }

    override operator fun get(id: Key): Provider<CommentedConfigurationNode> {
        return this.configProviders.getOrPut(id) {
            RootConfigProvider(resolvePath(id), id).also { provider ->
                if (this.lastReloadTimestamp > UNINITIALIZED_LAST_RELOAD) provider.reload()
            }
        }
    }

    override fun getOrNull(id: String): CommentedConfigurationNode? {
        val key = if (':' in id) Key.key(id) else Key.key(defaultNamespace(), id)
        return getOrNull(key)
    }

    override fun getOrNull(id: Key): CommentedConfigurationNode? {
        return this.configProviders[id]?.takeIf(RootConfigProvider::loaded)?.get()
    }

    @Deprecated("Needs code review before using it")
    override fun save(id: String) {
        val key = if (':' in id) Key.key(id) else Key.key(defaultNamespace(), id)
        save(key)
    }

    @Deprecated("Needs code review before using it")
    override fun save(id: Key) {
        val config = getOrNull(id) ?: return
        createLoader(id.namespace(), resolvePath(id)).save(config)
    }

    override fun registerSerializer(namespace: String, serializers: TypeSerializerCollection) {
        this.customSerializers.getOrPut(namespace, TypeSerializerCollection::builder).registerAll(serializers)
    }

    override fun <T> registerSerializer(namespace: String, type: TypeToken<T>, serializer: TypeSerializer<T>) {
        this.customSerializers.getOrPut(namespace, TypeSerializerCollection::builder).register(type, serializer)
    }

    override fun createBuilder(namespace: String): YamlConfigurationLoader.Builder {
        return YamlConfigurationLoader
            .builder()
            .nodeStyle(NodeStyle.BLOCK)
            .indent(2)
            .defaultOptions { opts ->
                opts.serializers(buildSerials(namespace))
            }
    }

    override fun createLoader(namespace: String, path: Path): YamlConfigurationLoader {
        return createBuilder(namespace).path(path).build()
    }
}
