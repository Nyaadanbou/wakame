package cc.mewcraft.wakame.attribute

/**
 * Holds one or more instances of [Attribute].
 */
interface AttributeProvider {
    /**
     * Gets an [Attribute] by its [descriptionId].
     */
    fun getSingleton(descriptionId: String): Attribute?

    /**
     * Gets a collection of [Attribute]s by [compositionId].
     *
     * **Remember that different [Attribute] instances may have the same composition id!**
     *
     * The returned list may contain zero or more attributes:
     * - `=0`: the composition is not registered
     * - `=1`: the composition is bound to exactly one attribute
     * - `>1`: the composition is bound to more than one attributes
     *
     * ## Side notes
     *
     * This function is primarily used by the config deserializer.
     *
     * @param compositionId the composition id
     * @return zero or more attributes
     */
    fun getComposition(compositionId: String): Collection<Attribute>

    /**
     * 检查 [descriptionId] 是否是一个元素属性.
     */
    fun isElementalByDescriptionId(descriptionId: String): Boolean

    /**
     * 检查 [compositionId] 是否是一个元素属性.
     */
    fun isElementalByCompositionId(compositionId: String): Boolean
}