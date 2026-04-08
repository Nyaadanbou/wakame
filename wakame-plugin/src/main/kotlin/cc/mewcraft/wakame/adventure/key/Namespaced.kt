package cc.mewcraft.wakame.adventure.key

import net.kyori.adventure.key.Namespaced

interface Namespaced : Namespaced {
    /**
     * @see namespace
     */
    val namespace: String

    override fun namespace(): String = namespace
}