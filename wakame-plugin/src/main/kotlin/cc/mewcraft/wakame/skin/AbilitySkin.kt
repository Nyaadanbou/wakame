package cc.mewcraft.wakame.skin

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.annotation.InternalApi
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

data class AbilitySkin @InternalApi internal constructor(
    override val key: String,
    override val binary: Short,
    override val displayName: String,
) : Skin, KoinComponent, BiIdentified<String, Short> {

    override val displayNameComponent: Component = get<MiniMessage>().deserialize(displayName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is AbilitySkin) return other.key == key
        return false
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}