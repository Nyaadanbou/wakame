package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.binary.WakaItemStack
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed

////// Interfaces //////

/**
 * Represents a condition testing whether a [cell][Cell] in a
 * [WakaItemStack] should be enabled or not. If a [cell][Cell] is
 * not enabled, it won't provide any effects as if it doesn't exist.
 */
interface Curse : Keyed {
    /**
     * The key of this lock condition. Used to identify the condition in the
     * context of binary and scheme item.
     */
    val key: Key
    override fun key(): Key = key

    companion object Constants {
        /**
         * The namespace of all types of curses.
         */
        const val NAMESPACE = "curse"
    }
}

////// Keys //////

object CurseKeys {
    val ENTITY_KILLS: Key = Key.key(Curse.NAMESPACE, "entity_kills")
    val PEAK_DAMAGE: Key = Key.key(Curse.NAMESPACE, "peak_damage")
}