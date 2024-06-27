package cc.mewcraft.wakame.item.components.cell

import cc.mewcraft.wakame.adventure.Keyed
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable

/**
 * A [core][Core] is a functional thing stored in a [cell][Cell].
 */
interface Core : Keyed, Examinable {
    /**
     * The key of the core, which must be unique among all other [cores][Core].
     */
    override val key: Key
}