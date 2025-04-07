package cc.mewcraft.wakame.entity.attribute

import org.jetbrains.annotations.ApiStatus

/**
 * Holds one or more instances of [Attribute].
 */
interface AttributeProvider {

    companion object {

        @get:JvmName("getInstance")
        @get:JvmStatic
        lateinit var INSTANCE: AttributeProvider private set

        @ApiStatus.Internal
        fun register(provider: AttributeProvider) {
            INSTANCE = provider
        }

    }

    /**
     * Gets an [Attribute] by its [id].
     */
    fun get(id: String): Attribute?

    /**
     * Gets a collection of [Attribute]s by [id].
     *
     * **Remember that different [Attribute] instances may have the same bundle id!**
     *
     * The returned list may contain zero or more attributes:
     * - `=0`: the bundle id is not registered
     * - `=1`: the bundle id is bound to exactly one attribute
     * - `>1`: the bundle id is bound to more than one attributes
     *
     * ## Side notes
     *
     * This function is primarily used by the config deserializer.
     *
     * @param id the bundle id
     * @return zero or more attributes
     */
    fun getList(id: String): Collection<Attribute>

    /**
     * 检查 [id] 是否是一个元素属性.
     */
    fun isElementalById(id: String): Boolean

    /**
     * 检查 [bundleId] 是否是一个元素属性.
     */
    fun isElementalByBundleId(bundleId: String): Boolean

}