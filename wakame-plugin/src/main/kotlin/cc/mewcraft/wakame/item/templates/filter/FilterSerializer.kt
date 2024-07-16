package cc.mewcraft.wakame.item.templates.filter

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.config.configurate.TypeDeserializer
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.EnumLookup
import cc.mewcraft.wakame.util.krequire
import com.google.common.collect.Range
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

internal object FilterSerializer : TypeDeserializer<Filter<GenerationContext>> {
    const val NAMESPACE_FILTER = "item"

    override fun deserialize(type: Type, node: ConfigurationNode): Filter<GenerationContext> {
        val rawType = node.node("type").krequire<Key>() // decoded as a Key, but we only focus on the Key#value()
        val inverted = node.node("invert").getBoolean(false) // check if we should invert the original result

        if (rawType.namespace() != NAMESPACE_FILTER) {
            throw SerializationException("The 'type' of filter must be in the '$NAMESPACE_FILTER' namespace")
        }

        val ret: Filter<GenerationContext> = when (rawType.value()) {
            "skill" -> {
                val key = node.node("key").krequire<Key>()
                FilterSkill(inverted, key)
            }

            "attribute" -> {
                val key = node.node("key").krequire<Key>()
                val operation = node.node("operation").krequire<String>().let { EnumLookup.lookup<AttributeModifier.Operation>(it).getOrThrow() }
                val element = node.node("element").get<Element>() // optional
                FilterAttribute(inverted, key, operation, element)
            }

            "curse" -> {
                val curse = node.node("key").krequire<Key>()
                FilterCurse(inverted, curse)
            }

            "element" -> {
                val element = node.node("element").krequire<Element>()
                FilterElement(inverted, element)
            }

            "item_level" -> {
                val level = node.node("level").krequire<Range<Int>>()
                FilterItemLevel(inverted, level)
            }

            "mark" -> {
                val meta = node.node("mark").krequire<String>()
                FilterMark(inverted, meta)
            }

            "rarity" -> {
                val rarity = node.node("rarity").krequire<Rarity>()
                FilterRarity(inverted, rarity)
            }

            "source_level" -> {
                val level = node.node("level").krequire<Range<Int>>()
                FilterSourceLevel(inverted, level)
            }

            "toss" -> {
                val chance = node.node("chance").krequire<Float>()
                FilterToss(inverted, chance)
            }

            else -> throw SerializationException("Can't recognize filter type '$rawType'")
        }

        return ret
    }
}