package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.registry.KizamiRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

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
     * The display name (MiniMessage string).
     */
    val displayName: String,
) : KoinComponent, BiIdentified<String, Byte> {

    private val mm: MiniMessage by inject(named(MINIMESSAGE_FULL))
    val displayNameComponent: Component = mm.deserialize(displayName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Kizami) return other.key == key
        return false
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}
