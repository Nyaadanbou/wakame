package cc.mewcraft.lazyconfig.access

import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
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

    protected val customSerializers = HashMap<String, TypeSerializerCollection.Builder>()
    protected val configProviders = HashMap<Key, RootConfigProvider>()
    protected var reloadCallback: ((List<Key>) -> Unit)? = null

    /**
     * 最后一次重新加载的时间戳.
     */
    protected var lastReloadTimestamp = UNINITIALIZED_LAST_RELOAD

    /**
     * 获取给定配置 ID 的配置文件路径。
     * 子类必须实现此方法以提供具体的路径解析逻辑。
     */
    protected abstract fun resolveConfigPath(configId: Key): Path

    override fun reload(): List<Key> {
        val reloadedConfigs = configProviders
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

        lastReloadTimestamp = System.currentTimeMillis()

        // 调用回调函数，让上层处理重新加载后的逻辑（如更新库存）
        reloadCallback?.invoke(reloadedConfigs)

        return reloadedConfigs
    }

    override operator fun get(id: String): Provider<CommentedConfigurationNode> {
        val key = if (':' in id) Key.key(id) else Key.key("koish", id)
        return get(key)
    }

    override operator fun get(id: Key): Provider<CommentedConfigurationNode> {
        return configProviders.getOrPut(id) {
            RootConfigProvider(resolveConfigPath(id), id).also { provider ->
                if (lastReloadTimestamp > UNINITIALIZED_LAST_RELOAD) provider.reload()
            }
        }
    }

    override fun getOrNull(id: String): CommentedConfigurationNode? {
        val key = if (':' in id) Key.key(id) else Key.key("koish", id)
        return getOrNull(key)
    }

    override fun getOrNull(id: Key): CommentedConfigurationNode? {
        return configProviders[id]?.takeIf(RootConfigProvider::loaded)?.get()
    }

    @Deprecated("Needs code review before using it")
    override fun save(id: String) {
        val key = if (':' in id) Key.key(id) else Key.key("koish", id)
        save(key)
    }

    @Deprecated("Needs code review before using it")
    override fun save(id: Key) {
        val config = getOrNull(id) ?: return
        createLoader(id.namespace(), resolveConfigPath(id)).save(config)
    }

    override fun registerSerializer(namespace: String, serializers: TypeSerializerCollection) {
        customSerializers.getOrPut(namespace, TypeSerializerCollection::builder).registerAll(serializers)
    }

    override fun <T> registerSerializer(namespace: String, type: TypeToken<T>, serializer: TypeSerializer<T>) {
        customSerializers.getOrPut(namespace, TypeSerializerCollection::builder).register(type, serializer)
    }

    override fun createBuilder(namespace: String): YamlConfigurationLoader.Builder {
        return YamlConfigurationLoader
            .builder()
            .defaultOptions { opts ->
                opts.serializers(buildSerializers(namespace))
            }
    }

    override fun createLoader(namespace: String, path: Path): YamlConfigurationLoader {
        return createBuilder(namespace).path(path).build()
    }

    override fun registerReload(callback: (List<Key>) -> Unit) {
        this.reloadCallback = callback
    }

    /**
     * 构建给定命名空间的 [TypeSerializerCollection].
     */
    protected open fun buildSerializers(namespace: String): TypeSerializerCollection {
        return TypeSerializerCollection.builder().build()
    }
}
