package cc.mewcraft.wakame.skin

import cc.mewcraft.wakame.BiIdentified
import cc.mewcraft.wakame.annotation.InternalApi
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.koin.core.component.KoinComponent

data class SkillVFX @InternalApi internal constructor(
    override val key: String,
    override val binary: Short,
    override val displayName: Component,
    override val styles: Array<StyleBuilderApplicable>,
) : Skin, KoinComponent, BiIdentified<String, Short> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is SkillVFX) return other.key == key
        return false
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun toString(): String {
        return PlainTextComponentSerializer.plainText().serialize(displayName)
    }
}