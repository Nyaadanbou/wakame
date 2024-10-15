package cc.mewcraft.wakame.attribute

/**
 * The interface for providing attributes.
 */
interface AttributeProvider {
    /**
     * Returns an attribute by the description ID.
     */
    fun getBy(descriptionId: String): Attribute?

    /**
     * Returns all description IDs.
     */
    fun allDescriptionId(): List<String>

    /**
     * Returns an empty attribute.
     */
    fun empty(): Attribute
}