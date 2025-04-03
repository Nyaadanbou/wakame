package cc.mewcraft.wakame.entity.attribute

import org.jetbrains.annotations.ApiStatus

/**
 * Provides the access to [AttributeMap] of various subjects.
 *
 * See the document of the subtypes for more details of usage.
 */
interface AttributeMapAccess {

    companion object Holder {

        @get:JvmName("getInstance()")
        lateinit var INSTANCE: AttributeMapAccess
            private set

        @Deprecated("", replaceWith = ReplaceWith("INSTANCE"))
        @JvmStatic
        fun instance(): AttributeMapAccess {
            return INSTANCE
        }

        @ApiStatus.Internal
        fun register(provider: AttributeMapAccess) {
            INSTANCE = provider
        }

    }

    /**
     * Gets the [AttributeMap] for the specified subject.
     */
    fun get(subject: Any): Result<AttributeMap>

}