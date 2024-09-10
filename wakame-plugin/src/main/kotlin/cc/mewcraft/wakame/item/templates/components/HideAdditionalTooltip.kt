package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import cc.mewcraft.wakame.item.components.HideAdditionalTooltip as HideAdditionalTooltipData


data object HideAdditionalTooltip : ItemTemplate<HideAdditionalTooltipData>, ItemTemplateBridge<HideAdditionalTooltip> {
    override val componentType: ItemComponentType<HideAdditionalTooltipData> = ItemComponentTypes.HIDE_ADDITIONAL_TOOLTIP

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<HideAdditionalTooltipData> {
        return ItemGenerationResult.of(HideAdditionalTooltipData.instance())
    }

    override fun codec(id: String): ItemTemplateType<HideAdditionalTooltip> {
        return Codec(id)
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<HideAdditionalTooltip> {
        override val type: TypeToken<HideAdditionalTooltip> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: {}
         * ```
         */
        override fun decode(node: ConfigurationNode): HideAdditionalTooltip {
            return HideAdditionalTooltip
        }
    }
}
