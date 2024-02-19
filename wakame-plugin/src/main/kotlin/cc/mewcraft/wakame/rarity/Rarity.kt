package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.registry.RarityRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

/**
 * **DO NOT CONSTRUCT IT YOURSELF!**
 *
 * Use [RarityRegistry] to get the instances instead.
 */
data class Rarity @InternalApi internal constructor(
    override val key: String,
    override val binary: Byte,
    /**
     * The display name (MiniMessage).
     */
    val displayName: String,
) : KoinComponent, BiIdentified<String, Byte> {

    private val mm: MiniMessage by inject(named(MINIMESSAGE_FULL))
    val displayNameComponent: Component = mm.deserialize(displayName)

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Rarity) return other.key == key
        return false
    }
}