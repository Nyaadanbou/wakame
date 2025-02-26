package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ShownInTooltip
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import cc.mewcraft.wakame.item.components.ItemAdventurePredicate as ItemAdventurePredicateData


data class ItemAdventurePredicate(
    override val componentType: ItemComponentType<ItemAdventurePredicateData>,
    override val showInTooltip: Boolean,
) : ItemTemplate<ItemAdventurePredicateData>, ShownInTooltip {
    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemAdventurePredicateData> {
        return ItemGenerationResult.of(ItemAdventurePredicateData(showInTooltip))
    }

    companion object : ItemTemplateBridge<ItemAdventurePredicate> {
        override fun codec(id: String): ItemTemplateType<ItemAdventurePredicate> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemAdventurePredicate> {
        override val type: TypeToken<ItemAdventurePredicate> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   show_in_tooltip: <boolean>
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemAdventurePredicate {
            val showInTooltip = node.node("show_in_tooltip").getBoolean(true)
            return when (id) {
                ItemConstants.CAN_BREAK -> ItemAdventurePredicate(ItemComponentTypes.CAN_BREAK, showInTooltip)
                ItemConstants.CAN_PLACE_ON -> ItemAdventurePredicate(ItemComponentTypes.CAN_PLACE_ON, showInTooltip)
                else -> throw IllegalArgumentException("Unknown template id: '$id'")
            }
        }
    }
}