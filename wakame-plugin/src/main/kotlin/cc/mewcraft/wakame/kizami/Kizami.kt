package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.FriendlyNamed
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.registry.KizamiRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.koin.core.component.KoinComponent

// TODO add more properties to this class such as what effects `this` kizami provides

/**
 * **DO NOT CONSTRUCT IT YOURSELF!**
 *
 * Use [KizamiRegistry] to get the instances instead.
 */
data class Kizami @InternalApi internal constructor(
    override val key: String,
    override val binary: Byte,
    override val displayName: Component,
    override val styles: Array<StyleBuilderApplicable>,
) : KoinComponent, FriendlyNamed, BiIdentified<String, Byte> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Kizami) return other.key == key
        return false
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun toString(): String {
        return PlainTextComponentSerializer.plainText().serialize(displayName)
    }
}
