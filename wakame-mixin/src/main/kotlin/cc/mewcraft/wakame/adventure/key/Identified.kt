package cc.mewcraft.wakame.adventure.key

import cc.mewcraft.wakame.util.Identifier
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed

interface Identified : Keyed {
    /**
     * @see identifier
     */
    val identifier: Identifier

    override fun key(): Key = identifier
}