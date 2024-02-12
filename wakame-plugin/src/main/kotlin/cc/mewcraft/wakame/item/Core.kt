package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.attribute.base.Attribute
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed

/**
 * Represents a functional thing in a [cell][Cell].
 *
 * Currently, the possible "functional things" are:
 * - [abilities][Ability]
 * - [attributes][Attribute]
 *
 * @see Cell
 */
interface Core : Keyed {
    /**
     * The key, which must be unique among all others of [cores][Core].
     */
    val key: Key

    override fun key(): Key = key
}
