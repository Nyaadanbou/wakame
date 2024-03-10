package cc.mewcraft.wakame.adventure

import net.kyori.adventure.key.Namespaced

interface Namespaced : Namespaced {
    /**
     * @see namespace
     */
    val namespace: String

    override fun namespace(): String = namespace
}