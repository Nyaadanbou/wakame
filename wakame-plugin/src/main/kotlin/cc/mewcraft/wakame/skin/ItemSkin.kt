package cc.mewcraft.wakame.skin

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.annotation.InternalApi
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

data class ItemSkin @InternalApi internal constructor(
    override val key: String,
    override val binary: Short,
    /**
     * The display name (MiniMessage string).
     */
    val displayName: String,
) : Skin, KoinComponent, BiIdentified<String, Short> {

    private val mm: MiniMessage by inject(named(MINIMESSAGE_FULL))
    val displayNameComponent: Component = mm.deserialize(displayName)

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