package cc.mewcraft.wakame.item.templates.filter

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random2.Filter
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.EnumLookup
import cc.mewcraft.wakame.util.krequire
import com.google.common.collect.Range
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException

object FilterFactory {
    // if the string starts with '~',
    // then we should create a filter
    // that always inverts its original result
    private const val NOT = '~'

    fun create(node: ConfigurationNode): Filter<GenerationContext> {
        val type0 = node.node("type").krequire<String>()
        val invert = type0.startsWith(NOT) // check if we should invert the original result
        val type = type0.substringAfter(NOT) // the type string (after ~)

        val ret: Filter<GenerationContext> = when (type) {
            "skill" -> {
                val key = node.node("key").krequire<Key>()
                FilterSkill(invert, key)
            }

            "attribute" -> {
                val key = node.node("key").krequire<Key>()
                val operation = node.node("operation").krequire<String>().let { EnumLookup.lookup<AttributeModifier.Operation>(it).getOrThrow() }
                val element = node.node("element").get<Element>() // optional
                FilterAttribute(invert, key, operation, element)
            }

            "curse" -> {
                val curse = node.node("key").krequire<Key>()
                FilterCurse(invert, curse)
            }

            "element" -> {
                val element = node.node("element").krequire<Element>()
                FilterElement(invert, element)
            }

            "item_level" -> {
                val level = node.node("level").krequire<Range<Int>>()
                FilterItemLevel(invert, level)
            }

            "mark" -> {
                val meta = node.node("mark").krequire<String>()
                FilterMark(invert, meta)
            }

            "rarity" -> {
                val rarity = node.node("rarity").krequire<Rarity>()
                FilterRarity(invert, rarity)
            }

            "source_level" -> {
                val level = node.node("level").krequire<Range<Int>>()
                FilterSourceLevel(invert, level)
            }

            "toss" -> {
                val chance = node.node("chance").krequire<Float>()
                FilterToss(invert, chance)
            }

            else -> throw SerializationException("Can't recognize filter type $type0")
        }

        return ret
    }
}