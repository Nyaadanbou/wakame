package cc.mewcraft.wakame.skin

import cc.mewcraft.wakame.BiIdentified

data class ItemSkin internal constructor(
    override val name: String,
    override val binary: Short,
    /**
     * The display name to players.
     */
    val displayName: String,
) : Skin, BiIdentified<String, Short> {
    override fun equals(other: Any?): Boolean {
        return if (other is ItemSkin) {
            other.name == name
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}