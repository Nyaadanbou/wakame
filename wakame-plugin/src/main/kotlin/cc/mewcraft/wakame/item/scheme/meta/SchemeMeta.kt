package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext

/**
 * 代表一个物品的元数据模板。
 *
 * **实现必须声明一个 `companion object` 并且实现 [net.kyori.adventure.key.Keyed].**
 *
 * @param T 模板最终产生的数据类型
 */
interface SchemeMeta<T : Any> {
    /**
     * Generate a value [T] from this scheme.
     *
     * A `null` value indicates that nothing is generated.
     *
     * ## Implementation Requirements
     * The implementation must populate relevant information about the
     * generated result into the [context].
     *
     * @param context the generation context
     * @return the generated [T]
     */
    fun generate(context: SchemeGenerationContext): T?
}