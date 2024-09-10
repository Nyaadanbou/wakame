package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.ShownInTooltip
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import cc.mewcraft.wakame.item.components.ItemDyeColor as ItemDyeColorData


data class ItemDyeColor(
    val rgb: Int,
    override val showInTooltip: Boolean,
) : ItemTemplate<ItemDyeColorData>, ShownInTooltip {
    override val componentType: ItemComponentType<ItemDyeColorData> = ItemComponentTypes.DYED_COLOR

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemDyeColorData> {
        return ItemGenerationResult.of(ItemDyeColorData(rgb, showInTooltip))
    }

    companion object : ItemTemplateBridge<ItemDyeColor> {
        override fun codec(id: String): ItemTemplateType<ItemDyeColor> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemDyeColor> {
        override val type: TypeToken<ItemDyeColor> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   show_in_tooltip: <boolean>
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemDyeColor {
            val rgb = node.node("rgb").int.takeIf { it in 0x000000..0xFFFFFF } ?: throw SerializationException(node, javaTypeOf<Int>(), "RGB value out of range")
            val showInTooltip = node.node("show_in_tooltip").getBoolean(true)
            return ItemDyeColor(
                rgb = rgb,
                showInTooltip = showInTooltip
            )
        }
    }
}
