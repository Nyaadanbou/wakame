package cc.mewcraft.wakame.skin

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.annotation.InternalApi

data class AbilitySkin @InternalApi internal constructor(
    override val key: String,
    override val binary: Short,
    /**
     * The display name to players.
     */
    val displayName: String,
) : Skin, BiIdentified<String, Short> {
    override fun equals(other: Any?): Boolean {
        return if (other is ItemSkin) {
            other.key == key
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}