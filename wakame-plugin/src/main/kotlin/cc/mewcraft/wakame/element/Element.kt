package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.registry.ElementRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

/**
 * **DO NOT CONSTRUCT IT YOURSELF!**
 *
 * Use [ElementRegistry] to get the instances instead.
 */
data class Element @InternalApi internal constructor(
    override val key: String,
    override val binary: Byte,
    /**
     * The display name (MiniMessage string).
     */
    val displayName: String,
) : KoinComponent, BiIdentified<String, Byte> {

    private val mm: MiniMessage by inject(named(MINIMESSAGE_FULL))
    val displayNameComponent: Component = mm.deserialize(displayName)

    // Instances of this class might be used as map keys
    // So we need to properly implement hashCode() and equals()
    // FIXME we might not need this because if we don't override hashCode/equals,
    //  the JVM will simply compare the objects by their references.
    //  All element instances are created at the pre-world stage,
    //  and no element instance will be created after that stage.

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Element) return other.key == key
        return false
    }
}