package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.BiIdentified

data class Kizami internal constructor(
    override val name: String,
    override val binary: Byte,
    /**
     * The display name to players.
     */
    val displayName: String,
) : BiIdentified<String, Byte> {
    override fun equals(other: Any?): Boolean {
        return if (other is Kizami) {
            other.name == name
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
