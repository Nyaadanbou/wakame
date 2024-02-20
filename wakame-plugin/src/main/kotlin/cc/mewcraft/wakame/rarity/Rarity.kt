package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.FriendlyNamed
import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.registry.RarityRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

/**
 * **DO NOT CONSTRUCT IT YOURSELF!**
 *
 * Use [RarityRegistry] to get the instances instead.
 */
data class Rarity @InternalApi internal constructor(
    override val key: String,
    override val binary: Byte,
    override val displayName: String,
) : KoinComponent, FriendlyNamed, BiIdentified<String, Byte> {

    override val displayNameComponent: Component = get<MiniMessage>(named(MINIMESSAGE_FULL)).deserialize(displayName)

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Rarity) return other.key == key
        return false
    }
}