package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext

/**
 * 代表一个物品的元数据模板。
 *
 * **实现必须声明一个 `companion object` 并且实现 [net.kyori.adventure.key.Keyed].**
 *
 * @param T 模板最终产生的数据类型
 */
sealed interface SchemeItemMeta<T> {
    /**
     * Generate a value [T] from this scheme.
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
    fun generate(context: SchemeGenerationContext): GenerationResult<T>
}
