package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.adventure.key.Keyed

interface Attribute : Keyed {

    val facadeId: String

    /**
     * 属性的名称ID.
     */
    val descriptionId: String

    /**
     * 属性的默认值.
     */
    val defaultValue: Double

    /**
     * 此属性是否为原版属性.
     */
    val vanilla: Boolean

    /**
     * 清理给定的数值，使其落在该属性的合理数值范围内。
     *
     * @param value 要清理的数值
     * @return 清理好的数值
     */
    fun sanitizeValue(value: Double): Double
}