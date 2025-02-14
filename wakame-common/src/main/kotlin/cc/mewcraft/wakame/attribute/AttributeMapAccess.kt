package cc.mewcraft.wakame.attribute

import org.jetbrains.annotations.ApiStatus

/**
 * Provides the access to [AttributeMap] of various subjects.
 *
 * See the document of the subtypes for more details of usage.
 */
interface AttributeMapAccess {

    companion object Holder {

        private var instance: AttributeMapAccess? = null

        @JvmStatic
        fun instance(): AttributeMapAccess {
            return instance ?: throw IllegalStateException("AttributeMapAccess has not been initialized")
        }

        @ApiStatus.Internal
        fun register(provider: AttributeMapAccess) {
            instance = provider
        }

        @ApiStatus.Internal
        fun unregister() {
            instance = null
        }

    }

    /**
     * Gets the [AttributeMap] for the specified subject.
     */
    fun get(subject: Any): Result<AttributeMap>

}