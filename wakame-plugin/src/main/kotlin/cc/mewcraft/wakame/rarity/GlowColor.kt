package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.text.format.NamedTextColor
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.ScalarSerializer
import java.lang.reflect.Type
import java.util.function.Predicate

sealed interface GlowColor {
    val color: NamedTextColor

    companion object {
        fun empty(): GlowColor = EmptyGlowColor
        fun of(color: NamedTextColor): GlowColor = GlowColorImpl(color)
    }
}

private data object EmptyGlowColor : GlowColor {
    override val color: NamedTextColor = NamedTextColor.WHITE
}

private data class GlowColorImpl(override val color: NamedTextColor) : GlowColor {
    override fun toString(): String = color.toString()
}

internal object GlowColorSerializer : ScalarSerializer<GlowColor>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): GlowColor {
        return when (obj) {
            is NamedTextColor -> GlowColor.of(obj)
            is String -> NamedTextColor.NAMES.value(obj.lowercase())?.let { GlowColor.of(it) } ?: throw IllegalArgumentException("Unknown color: $obj")
            else -> throw IllegalArgumentException("Unknown color: $obj")
        }
    }

    override fun serialize(item: GlowColor?, typeSupported: Predicate<Class<*>>?): Any {
        throw UnsupportedOperationException()
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): GlowColor {
        return GlowColor.empty()
    }
}