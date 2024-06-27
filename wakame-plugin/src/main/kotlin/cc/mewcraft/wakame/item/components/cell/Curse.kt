package cc.mewcraft.wakame.item.components.cell

import cc.mewcraft.wakame.adventure.Keyed
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable

/**
 * Represents a condition testing whether a [cell][Cell] in an
 * item should be enabled or not. If a [cell][Cell] is not enabled,
 * it won't provide any effects as if it doesn't exist.
 */
interface Curse : Keyed, Examinable {
    /**
     * The key of this lock condition. Used to identify the condition in the
     * context of binary and schema item.
     */
    override val key: Key
}