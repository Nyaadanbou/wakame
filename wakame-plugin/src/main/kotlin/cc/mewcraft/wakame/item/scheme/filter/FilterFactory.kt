package cc.mewcraft.wakame.item.scheme.filter

import cc.mewcraft.spatula.utils.RangeParser
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException

object FilterFactory {
    fun create(node: ConfigurationNode): Filter {
        val type0 = node.node("type").requireKt<String>()

        // if the string starts with '~',
        // then we should create a filter
        // that always inverts its original result
        val not = '~'

        val invert = type0.startsWith(not) // check if we should invert the original result
        val type = type0.substringAfter(not) // the type string (after ~)

        val ret: Filter = when (type) {
            "element" -> {
                val element = node.node("element").requireKt<Element>()
                ElementFilter(invert, element)
            }

            "item_level" -> {
                val level = node.node("level").requireKt<String>().let { RangeParser.parseIntRange(it) }
                ItemLevelFilter(invert, level)
            }

            "mark" -> {
                val meta = node.node("mark").requireKt<String>()
                MarkFilter(invert, meta)
            }

            "player_level" -> {
                val level = node.node("level").requireKt<String>().let { RangeParser.parseIntRange(it) }
                PlayerLevelFilter(invert, level)
            }

            "rarity" -> {
                val rarity = node.node("rarity").requireKt<Rarity>()
                RarityFilter(invert, rarity)
            }

            "core" -> {
                val coreKey = node.node("key").requireKt<Key>()
                CoreFilter(invert, coreKey)
            }

            "toss" -> {
                val chance = node.node("chance").requireKt<Float>()
                TossFilter(invert, chance)
            }

            else -> throw SerializationException("Can't recognize filter type $type0")
        }

        return ret
    }
}