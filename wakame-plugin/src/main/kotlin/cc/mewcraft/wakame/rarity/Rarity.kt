package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.BiIdentified

data class Rarity internal constructor(
    override val name: String,
    override val binary: Byte,
    /**
     * The display name to players.
     */
    val displayName: String,
) : BiIdentified<String, Byte> {
    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is Rarity) {
            return other.name == name
        }
        return false
    }
}