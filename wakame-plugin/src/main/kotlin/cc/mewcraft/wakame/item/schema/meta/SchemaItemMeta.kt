package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.item.ItemMeta
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext

/**
 * 代表一个物品的元数据模板。
 *
 * ## 实现模板
 *
 * 以下是一个实现的模板，其中要修改的地方已经标记为 `???`。
 *
 * ```kotlin
 * // 创建的接口必须有 `ConfigPath` 注解，并且传入配置文件中的路径
 * @ConfigPath(ItemMetaConstants.???)
 *
 * // 先创建一个接口，继承关系如下，并且需要重写 `key`，以避免之后写重复代码
 * // 其中 `key` 的取值应该从固定的地方获取，而不是直接在这里写个新的字符串
 * sealed interface S???Meta : SchemaItemMeta<???> {
 *     override val key: Key get() = ItemMetaConstants.createKey { ??? }
 * }
 *
 * // 当配置文件里存在该 ItemMeta 时，应该创建该实例
 * private class NonNull???Meta(
 *     private val ???, // 构造器参数，看情况写
 * ) : S???Meta {
 *     override val isEmpty: Boolean = false // 因为这是一个 NonNull 的实例，因此永远为 `false`
 *     override fun generate(context: SchemaGenerationContext): GenerationResult<???> {
 *         return ???
 *     }
 * }
 *
 * // 当配置文件里不存在该 ItemMeta，应该创建该实例（代表默认值）
 * private data object Default???Meta : S???Meta {
 *     override val isEmpty: Boolean = ??? // 取值取决于该默认值是否会产生数据
 *     override fun generate(context: SchemaGenerationContext): GenerationResult<String> {
 *         return ???
 *     }
 * }
 *
 * // 创建该 ItemMeta 对应的配置文件序列化实现
 * internal data object ???MetaSerializer : SchemaItemMetaSerializer<S???Meta> {
 *     override val defaultValue: S???Meta = Default???Meta
 *     override fun deserialize(type: Type, node: ConfigurationNode): S???Meta {
 *         return ???
 *     }
 * }
 * ```
 *
 * @param T 模板最终产生的数据类型
 */
sealed interface SchemaItemMeta<T> : ItemMeta {
    /**
     * Checks whether the schema will generate anything or not.
     *
     * Returning `true` means the schema will generate something AND the generated data
     * will be written to the ItemStack; `false` otherwise.
     */
    val isEmpty: Boolean

    /**
     * Generate a value [T] from this schema.
     *
     * **Returning [GenerationResult.empty] indicates that the item meta
     * should not be written to the ItemStack.**
     *
     * The implementation must populate the [context] with relevant
     * information about the generation result.
     *
     * @param context the generation context
     * @return the generated [T]
     */
    fun generate(context: SchemaGenerationContext): GenerationResult<T>

}

