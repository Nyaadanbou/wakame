package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.item.binary.NekoStack
import net.kyori.adventure.key.Key

/**
 * Represents a condition testing whether a [cell][Cell] in a
 * [NekoStack] should be enabled or not. If a [cell][Cell] is
 * not enabled, it won't provide any effects as if it doesn't exist.
 */
interface Curse : Keyed {
    /**
     * The key of this lock condition. Used to identify the condition in the
     * context of binary and schema item.
     */
    override val key: Key
}

/**
 * The keys of [curses][Curse].
 */
object CurseKeys {
    val ENTITY_KILLS: Key = Key.key(NekoNamespaces.CURSE, "entity_kills")
    val PEAK_DAMAGE: Key = Key.key(NekoNamespaces.CURSE, "peak_damage")
}