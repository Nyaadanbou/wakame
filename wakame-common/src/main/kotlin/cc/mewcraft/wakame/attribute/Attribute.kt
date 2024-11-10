package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.adventure.key.Keyed

/**
 * 代表一种 *属性*. 一般作为静态常量使用.
 *
 * 使用 [AttributeProvider] 来获取实例.
 */
interface Attribute : Keyed {
    /**
     * 本属性所属的融合属性的唯一标识.
     */
    val compositionId: String

    /**
     * 本属性的唯一标识.
     */
    val descriptionId: String

    /**
     * 本属性的默认值.
     */
    val defaultValue: Double

    /**
     * 本属性是否为原版属性.
     */
    val vanilla: Boolean

    /**
     * 使 [value] 落在本属性规定的数值范围之内.
     *
     * @param value 待清理的数值
     * @return 清理好的数值
     */
    fun sanitizeValue(value: Double): Double
}