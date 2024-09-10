package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import cc.mewcraft.wakame.item.components.HideTooltip as HideTooltipData


data object HideTooltip : ItemTemplate<HideTooltipData>, ItemTemplateBridge<HideTooltip> {
    override val componentType: ItemComponentType<HideTooltipData> = ItemComponentTypes.HIDE_TOOLTIP

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<HideTooltipData> {
        return ItemGenerationResult.of(HideTooltipData.instance())
    }

    override fun codec(id: String): ItemTemplateType<HideTooltip> {
        return Codec(id)
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<cc.mewcraft.wakame.item.templates.components.HideTooltip> {
        override val type: TypeToken<cc.mewcraft.wakame.item.templates.components.HideTooltip> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: {}
         * ```
         */
        override fun decode(node: ConfigurationNode): cc.mewcraft.wakame.item.templates.components.HideTooltip {
            return HideTooltip
        }
    }
}
