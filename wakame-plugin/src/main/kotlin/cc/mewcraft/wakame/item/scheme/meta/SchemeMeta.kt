package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext

/**
 * 代表一个物品的元数据模板。
 *
 * @param T 模板最终产生的数据类型
 */
interface SchemeMeta<T : Any> {
    /*
      注意，这代表一个模板数据，也就是配置文件内容的表现
      数值储存上可能需要支持正态分布 (NumericValue)
    */

    /**
     * Generate a value [T] from this scheme.
     *
     * A `null` value indicates that nothing is generated.
     *
     * @param context the generation context
     * @return the generated [T]
     */
    fun generate(context: SchemeGenerationContext): T?
}