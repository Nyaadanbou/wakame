package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.registry.ElementRegistry

/**
 * **DO NOT CONSTRUCT IT YOURSELF!**
 *
 * Use [ElementRegistry] to get the instances instead.
 */
data class Element internal constructor(
    override val name: String,
    override val binary: Byte,
    /**
     * The display name to players.
     */
    val displayName: String,
) : BiIdentified<String, Byte> {
    // Instances of this class might be used as map keys
    // So we need to properly implement hashCode() and equals()
    // FIXME we might not need this because if we don't override hashCode/equals
    //  the JVM will simply compare the objects by their reference
    //  All element instances are created at the pre-world stage,
    //  and no element instance will be created after that stage.

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is Element) {
            return other.name == name
        }
        return false
    }
}