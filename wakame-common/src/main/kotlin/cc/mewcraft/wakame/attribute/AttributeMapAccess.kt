package cc.mewcraft.wakame.attribute

/**
 * Provides the access to [AttributeMap] of various subjects.
 *
 * See the document of the subtypes for more details of usage.
 */
interface AttributeMapAccess {
    /**
     * Gets the [AttributeMap] for the specified subject.
     */
    fun get(subject: Any): Result<AttributeMap>
}