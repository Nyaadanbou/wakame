package cc.mewcraft.wakame.config

import cc.mewcraft.wakame.feature.Feature
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.jetbrains.annotations.ApiStatus
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import xyz.xenondevs.commons.provider.Provider
import java.nio.file.Path

/**
 * 插件的主要 `config.yml`.
 */
val MAIN_CONFIG: Provider<CommentedConfigurationNode> = ConfigAccess.INSTANCE["config"]

/**
 * 注册自定义 [serializer] 用于 [feature] 的配置文件.
 */
inline fun <reified T> ConfigAccess.registerSerializer(feature: Feature, serializer: TypeSerializer<T>) {
    ConfigAccess.INSTANCE.registerSerializer(feature, typeTokenOf(), serializer)
}

/**
 * 注册自定义 [serializer] 用于 [namespace] 的配置文件.
 */
inline fun <reified T> ConfigAccess.registerSerializer(namespace: String, serializer: TypeSerializer<T>) {
    ConfigAccess.INSTANCE.registerSerializer(namespace, typeTokenOf(), serializer)
}

/**
 * 用于访问配置文件的 [Provider].
 */
interface ConfigAccess {

    companion object {

        @get:JvmStatic
        @get:JvmName("getInstance")
        lateinit var INSTANCE: ConfigAccess private set

        @ApiStatus.Internal
        fun register(instance: ConfigAccess) {
            this.INSTANCE = instance
        }

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
     *
     * @param feature 对应的 [Feature] (仅取其命名空间)
     * @param path 相对于 [feature] 文件夹的文件路径
     */
    operator fun get(feature: Feature, path: String): Provider<CommentedConfigurationNode>

    /**
     * 返回指定配置文件的 [Provider].
     */
    operator fun get(id: Identifier): Provider<CommentedConfigurationNode>

    /**
     * 返回指定配置文件的 [Provider].
     *
     * @param id 配置文件的 id, 必须是 `namespace:path` 的形式
     */
    fun getOrNull(id: String): CommentedConfigurationNode?

    /**
     * 返回指定配置文件的 [Provider].
     */
    fun getOrNull(id: Identifier): CommentedConfigurationNode?

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
    fun save(id: Identifier)

    /**
     * 注册自定义 [serializers] 用于 [feature] 的配置文件.
     */
    fun registerSerializer(feature: Feature, serializers: TypeSerializerCollection)

    /**
     * 注册自定义 [serializers] 用于 [namespace] 的配置文件.
     */
    fun registerSerializer(namespace: String, serializers: TypeSerializerCollection)

    /**
     * 注册自定义 [serializer] 用于 [feature] 的配置文件.
     */
    fun <T> registerSerializer(feature: Feature, type: TypeToken<T>, serializer: TypeSerializer<T>)

    /**
     * 注册自定义 [serializer] 用于 [namespace] 的配置文件.
     */
    fun <T> registerSerializer(namespace: String, type: TypeToken<T>, serializer: TypeSerializer<T>)

    // ------------
    // Internal API
    // ------------

    @ApiStatus.Internal
    fun createBuilder(namespace: String): YamlConfigurationLoader.Builder

    @ApiStatus.Internal
    fun createLoader(namespace: String, path: Path): YamlConfigurationLoader

}