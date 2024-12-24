package cc.mewcraft.wakame.item.templates.filters

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.krequire
import com.google.common.collect.Range
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

internal object FilterSerializer : TypeSerializer<Filter<ItemGenerationContext>> {
    const val NAMESPACE_FILTER = "item"

    override fun deserialize(type: Type, node: ConfigurationNode): Filter<ItemGenerationContext> {
        val rawType = node.node("type").krequire<Key>() // decoded as a Key, but we only focus on the Key#value()
        val inverted = node.node("invert").getBoolean(false) // check if we should invert the original result

        val ret: Filter<ItemGenerationContext> = when (rawType) {
            AbilityFilter.TYPE -> {
                val key = node.node("ability").krequire<Key>()
                AbilityFilter(inverted, key)
            }

            AttributeFilter.TYPE -> {
                val id = node.node("attribute").krequire<String>()
                val operation = node.node("operation").get<AttributeModifier.Operation>()
                val element = node.node("element").get<Element>() // optional
                AttributeFilter(inverted, id, operation, element)
            }

            ElementFilter.TYPE -> {
                val element = node.node("element").krequire<Element>()
                ElementFilter(inverted, element)
            }

            ItemLevelFilter.TYPE -> {
                val level = node.node("level").krequire<Range<Int>>()
                ItemLevelFilter(inverted, level)
            }

            MarkFilter.TYPE -> {
                val meta = node.node("mark").krequire<String>()
                MarkFilter(inverted, meta)
            }

            RarityFilter.TYPE -> {
                val rarity = node.node("rarity").krequire<Rarity>()
                RarityFilter(inverted, rarity)
            }

            SourceLevelFilter.TYPE -> {
                val level = node.node("level").krequire<Range<Int>>()
                SourceLevelFilter(inverted, level)
            }

            TossFilter.TYPE -> {
                val chance = node.node("chance").krequire<Float>()
                TossFilter(inverted, chance)
            }

            else -> throw SerializationException("Can't recognize filter type '$rawType'")
        }

        return ret
    }
}