package cc.mewcraft.lazyconfig.access

import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.ApiStatus
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import xyz.xenondevs.commons.provider.Provider
import java.nio.file.Path


/**
 * 用于访问配置文件的 [Provider].
 */
interface ConfigAccess {

    companion object : ConfigAccess {

        private lateinit var instance: ConfigAccess

        fun setImplementation(instance: ConfigAccess) {
            this.instance = instance
        }

        override fun get(id: String): Provider<CommentedConfigurationNode> = instance[id]
        override fun get(id: Key): Provider<CommentedConfigurationNode> = instance[id]
        override fun getOrNull(id: String): CommentedConfigurationNode? = instance.getOrNull(id)
        override fun getOrNull(id: Key): CommentedConfigurationNode? = instance.getOrNull(id)
        @Deprecated("Needs code review before using it")
        override fun save(id: String): Unit = instance.save(id)
        @Deprecated("Needs code review before using it")
        override fun save(id: Key): Unit = instance.save(id)
        override fun registerSerializer(namespace: String, serializers: TypeSerializerCollection): Unit = instance.registerSerializer(namespace, serializers)
        override fun <T> registerSerializer(namespace: String, type: TypeToken<T>, serializer: TypeSerializer<T>): Unit = instance.registerSerializer(namespace, type, serializer)
        override fun reload(): List<Key> = instance.reload()
        override fun createBuilder(namespace: String): YamlConfigurationLoader.Builder = instance.createBuilder(namespace)
        override fun createLoader(namespace: String, path: Path): YamlConfigurationLoader = instance.createLoader(namespace, path)
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
    operator fun get(id: String): Provider<CommentedConfigurationNode>

    /**
     * 返回指定配置文件的 [Provider].
     */
    operator fun get(id: Key): Provider<CommentedConfigurationNode>

    /**
     * 返回指定配置文件的 [Provider].
     *
     * @param id 配置文件的 id, 必须是 `namespace:path` 的形式
     */
    fun getOrNull(id: String): CommentedConfigurationNode?

    /**
     * 返回指定配置文件的 [Provider].
     */
    fun getOrNull(id: Key): CommentedConfigurationNode?

    /**
     * 保存指定配置文件.
     *
     * @param id 配置文件的 id, 必须是 `namespace:path` 的形式
     */
    @Deprecated("Needs code review before using it")
    fun save(id: String)

    /**
     * 保存指定配置文件.
     */
    @Deprecated("Needs code review before using it")
    fun save(id: Key)

    /**
     * 注册自定义 [serializers] 用于 [namespace] 的配置文件.
     */
    fun registerSerializer(namespace: String, serializers: TypeSerializerCollection)

    /**
     * 注册自定义 [serializer] 用于 [namespace] 的配置文件.
     */
    fun <T> registerSerializer(namespace: String, type: TypeToken<T>, serializer: TypeSerializer<T>)

    // ------------
    // Internal API
    // ------------

    @ApiStatus.Internal
    fun reload(): List<Key>

    @ApiStatus.Internal
    fun createBuilder(namespace: String): YamlConfigurationLoader.Builder

    @ApiStatus.Internal
    fun createLoader(namespace: String, path: Path): YamlConfigurationLoader
}

inline fun <reified T> ConfigAccess.registerSerializer(namespace: String, serializer: TypeSerializer<T>) {
    registerSerializer(namespace, typeTokenOf(), serializer)
}

@PublishedApi
internal inline fun <reified T> typeTokenOf(): TypeToken<T> = object : TypeToken<T>() {}
