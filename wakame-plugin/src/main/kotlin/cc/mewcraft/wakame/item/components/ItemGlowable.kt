package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.display2.RendererSystemName
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode

interface ItemGlowable : Examinable, TooltipProvider.Single {
    companion object : ItemComponentBridge<ItemGlowable>, ItemComponentMeta {
        fun of(): ItemGlowable {
            return Value
        }

        override fun codec(id: String): ItemComponentType<ItemGlowable> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemConstants.GLOWABLE
        override val tooltipKey: Key = ItemConstants.createKey { GLOWABLE }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
        private val tooltip: ItemComponentConfig.SingleTooltip = config.SingleTooltip()
    }

    private data object Value : ItemGlowable {
        override fun provideTooltipLore(systemName: RendererSystemName): LoreLine {
            if (!config.showInTooltip) {
                return LoreLine.noop()
            }

            val rendered = tooltip.render(systemName) ?: return LoreLine.noop()
            return LoreLine.simple(tooltipKey, listOf(rendered))
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemGlowable> {
        override fun read(holder: ItemComponentHolder): ItemGlowable? {
            return if (holder.hasTag()) Value else null
        }

        override fun write(holder: ItemComponentHolder, value: ItemGlowable) {
            holder.editTag()
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }
    }

    data object Template : ItemTemplate<ItemGlowable> {
        override val componentType: ItemComponentType<ItemGlowable> = ItemComponentTypes.GLOWABLE

        override fun generate(context: GenerationContext): GenerationResult<ItemGlowable> {
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