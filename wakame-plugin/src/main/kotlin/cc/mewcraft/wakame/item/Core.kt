package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.adventure.Keyed
import net.kyori.adventure.key.Key

/**
 * A [core][Core] is a functional thing stored in a [cell][Cell].
 */
interface Core : Keyed {
    /**
     * The key of the core, which must be unique among all other [cores][Core].
     */
    override val key: Key
}