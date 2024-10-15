package cc.mewcraft.wakame.attribute

/**
 * Provides the access to [AttributeMap] of various subjects.
 *
 * See the document of the subtypes for more details of usage.
 */
interface AttributeAccessor<T> {
    /**
     * Gets the [AttributeMap] for the [subject].
     */
    fun getAttributeMap(subject: T): AttributeMap
}