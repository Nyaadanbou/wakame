package cc.mewcraft.wakame.entity.attribute

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus

/**
 * Provides the access to [AttributeMap] of various subjects.
 *
 * See the document of the subtypes for more details of usage.
 */
interface AttributeMapAccess {

    companion object {

        @get:JvmStatic
        @get:JvmName("getInstance()")
        lateinit var INSTANCE: AttributeMapAccess private set

        @ApiStatus.Internal
        fun register(provider: AttributeMapAccess) {
            INSTANCE = provider
        }

    }

    /**
     * Gets the [AttributeMap] for the specified player.
     */
    fun get(player: Player): Result<AttributeMap>

    /**
     * Gets the [AttributeMap] for the specified entity.
     */
    fun get(entity: Entity): Result<AttributeMap>

}