package cc.mewcraft.wakame.attribute

/**
 * Provides the access to [AttributeMap] of various subjects.
 *
 * See the document of the subtypes for more details of usage.
 */
interface AttributeMapAccess<T> {
    /**
     * Gets the [AttributeMap] for the [subject].
     */
    fun get(subject: T): AttributeMap

    companion object Constants {
        const val FOR_PLAYER = "for_player"
        const val FOR_ENTITY = "for_entity"
    }
}