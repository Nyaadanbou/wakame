package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import cc.mewcraft.wakame.item.components.ItemAttributeModifiers as ItemAttributeModifiersData


data class ItemAttributeModifiers(
    val showInTooltip: Boolean,
) : ItemTemplate<ItemAttributeModifiersData> {
    override val componentType: ItemComponentType<ItemAttributeModifiersData> = ItemComponentTypes.ATTRIBUTE_MODIFIERS

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemAttributeModifiersData> {
        return ItemGenerationResult.of(ItemAttributeModifiersData(showInTooltip))
    }

    companion object : ItemTemplateBridge<ItemAttributeModifiers> {
        override fun codec(id: String): ItemTemplateType<ItemAttributeModifiers> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemAttributeModifiers> {
        override val type: TypeToken<ItemAttributeModifiers> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   show_in_tooltip: <boolean>
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemAttributeModifiers {
            val showInTooltip = node.node("show_in_tooltip").getBoolean(true)
            return ItemAttributeModifiers(showInTooltip)
        }
    }
}