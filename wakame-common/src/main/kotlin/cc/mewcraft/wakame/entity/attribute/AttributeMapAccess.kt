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
     * 返回指定 [player] 的 [AttributeMap].
     */
    fun get(player: Player): AttributeMap

    /**
     * 返回指定 [entity] 的 [AttributeMap].
     *
     * 如果 [entity] 实际上是 [Player] 类型则会自动调用 `get(Player)`.
     */
    fun get(entity: Entity): Result<AttributeMap>

}