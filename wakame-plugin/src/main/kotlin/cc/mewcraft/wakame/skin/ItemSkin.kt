package cc.mewcraft.wakame.skin

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.annotation.InternalApi
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

data class ItemSkin @InternalApi internal constructor(
    override val key: String,
    override val binary: Short,
    override val displayName: String,
) : Skin, KoinComponent, BiIdentified<String, Short> {

    override val displayNameComponent: Component = get<MiniMessage>(named(MINIMESSAGE_FULL)).deserialize(displayName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is ItemSkin) return other.key == key
        return false
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}