package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentMeta
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.bukkit.inventory.ItemFlag
import org.spongepowered.configurate.ConfigurationNode

interface HideAdditionalTooltip : Examinable {

    companion object : ItemComponentBridge<HideAdditionalTooltip>, ItemComponentMeta {
        /**
         * 返回 [HideAdditionalTooltip] 的实例.
         */
        fun of(): HideAdditionalTooltip {
            return Value
        }

        override fun codec(id: String): ItemComponentType<HideAdditionalTooltip> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemConstants.HIDE_ADDITIONAL_TOOLTIP
        override val tooltipKey: Key = ItemConstants.createKey { HIDE_ADDITIONAL_TOOLTIP }
    }

    private data object Value : HideAdditionalTooltip

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<HideAdditionalTooltip> {
        override fun read(holder: ItemComponentHolder): HideAdditionalTooltip? {
            val im = holder.item.itemMeta ?: return null
            if (im.hasItemFlag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)) {
                return Value
            }
            return null
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta {
                it.removeItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            }
        }

        override fun write(holder: ItemComponentHolder, value: HideAdditionalTooltip) {
            holder.item.editMeta {
                it.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            }
        }
    }

    data object Template : ItemTemplate<HideAdditionalTooltip> {
        override val componentType: ItemComponentType<HideAdditionalTooltip> = ItemComponentTypes.HIDE_ADDITIONAL_TOOLTIP

        override fun generate(context: GenerationContext): GenerationResult<HideAdditionalTooltip> {
            return GenerationResult.of(Value)
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template> {
        override val type: TypeToken<Template> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: {}
         * ```
         */
        override fun decode(node: ConfigurationNode): Template {
            return Template
        }
    }

}