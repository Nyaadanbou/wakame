package cc.mewcraft.wakame.adventure

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed

interface Keyed : Keyed {
    /**
     * @see key
     */
    val key: Key

    override fun key(): Key = key
}