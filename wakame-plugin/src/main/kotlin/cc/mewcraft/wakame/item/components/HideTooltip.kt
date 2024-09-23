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
import org.spongepowered.configurate.ConfigurationNode

interface HideTooltip : Examinable {

    companion object : ItemComponentBridge<HideTooltip>, ItemComponentMeta {
        /**
         * 返回 [HideTooltip] 的实例.
         */
        fun of(): HideTooltip {
            return Value
        }

        override fun codec(id: String): ItemComponentType<HideTooltip> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemConstants.HIDE_TOOLTIP
        override val tooltipKey: Key = ItemConstants.createKey { HIDE_TOOLTIP }
    }

    private data object Value : HideTooltip

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<HideTooltip> {
        override fun read(holder: ItemComponentHolder): HideTooltip? {
            val im = holder.item.itemMeta ?: return null
            if (im.isHideTooltip) {
                return Value
            }
            return null
        }

        override fun write(holder: ItemComponentHolder, value: HideTooltip) {
            holder.item.editMeta {
                it.isHideTooltip = true
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta {
                it.isHideTooltip = false
            }
        }
    }

    data object Template : ItemTemplate<HideTooltip> {
        override val componentType: ItemComponentType<HideTooltip> = ItemComponentTypes.HIDE_TOOLTIP

        override fun generate(context: GenerationContext): GenerationResult<HideTooltip> {
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