package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.item.ItemMeta
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext

/**
 * 代表一个物品的元数据模板。
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
