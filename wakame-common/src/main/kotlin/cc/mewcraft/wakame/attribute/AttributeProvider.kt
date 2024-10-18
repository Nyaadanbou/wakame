package cc.mewcraft.wakame.attribute

/**
 * The interface for getting instances of [Attribute].
 */
interface AttributeProvider {
    /**
     * 返回所有已知的 [Attribute.descriptionId] (包括元素属性的).
     */
    val descriptionIds: Set<String>

    /**
     * 返回一个 [descriptionId] 对应的 [Attribute] 实例.
     */
    fun getBy(descriptionId: String): Attribute?
}