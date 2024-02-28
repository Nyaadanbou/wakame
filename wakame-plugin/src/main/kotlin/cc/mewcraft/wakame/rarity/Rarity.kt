package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.FriendlyNamed
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.registry.RarityRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.koin.core.component.KoinComponent

/**
 * **DO NOT CONSTRUCT IT YOURSELF!**
 *
 * Use [RarityRegistry] to get the instances instead.
 */
data class Rarity @InternalApi internal constructor(
    override val key: String,
    override val binary: Byte,
    override val displayName: Component,
    override val styles: Array<StyleBuilderApplicable>,
) : KoinComponent, FriendlyNamed, BiIdentified<String, Byte> {
    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Rarity) return other.key == key
        return false
    }

    override fun toString(): String {
        return PlainTextComponentSerializer.plainText().serialize(displayName)
    }
}