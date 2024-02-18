package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.registry.RarityRegistry

/**
 * **DO NOT CONSTRUCT IT YOURSELF!**
 *
 * Use [RarityRegistry] to get the instances instead.
 */
data class Rarity @InternalApi internal constructor(
    override val key: String,
    override val binary: Byte,
    /**
     * The display name to players.
     */
    val displayName: String,
) : BiIdentified<String, Byte> {
    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is Rarity) {
            return other.key == key
        }
        return false
    }
}