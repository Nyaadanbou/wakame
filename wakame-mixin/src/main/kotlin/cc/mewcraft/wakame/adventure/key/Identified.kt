package cc.mewcraft.wakame.adventure.key

import cc.mewcraft.wakame.util.KoishKey
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed

interface Identified : Keyed {
    /**
     * @see key
     */
    val key: KoishKey

    override fun key(): Key = key
}