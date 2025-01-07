package cc.mewcraft.wakame.skin

import cc.mewcraft.wakame.BiIdentifiable
import cc.mewcraft.wakame.annotation.InternalApi
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.koin.core.component.KoinComponent

data class ItemSkin @InternalApi internal constructor(
    override val uniqueId: String,
    override val binaryId: Short,
    override val displayName: Component,
    override val displayStyles: Array<StyleBuilderApplicable>,
    /**
     * K - predicate name
     * V - variant
     */
    val predicates: Map<String, Int>
) : Skin, KoinComponent, BiIdentifiable<String, Short> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is ItemSkin) return other.uniqueId == uniqueId
        return false
    }

    override fun hashCode(): Int {
        return uniqueId.hashCode()
    }

    override fun toString(): String {
        return PlainTextComponentSerializer.plainText().serialize(displayName)
    }
}