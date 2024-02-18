package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.registry.KizamiRegistry

// TODO add more properties to this class
//  such as what effects `this` kizami provides

/**
 * **DO NOT CONSTRUCT IT YOURSELF!**
 *
 * Use [KizamiRegistry] to get the instances instead.
 */
data class Kizami @InternalApi internal constructor(
    override val key: String,
    override val binary: Byte,
    /**
     * The display name to players.
     */
    val displayName: String,
) : BiIdentified<String, Byte> {
    override fun equals(other: Any?): Boolean {
        return if (other is Kizami) {
            other.key == key
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}
