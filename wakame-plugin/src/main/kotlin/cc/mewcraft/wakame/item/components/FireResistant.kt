package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
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
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface FireResistant : Examinable, TooltipProvider.Single {

    companion object : ItemComponentBridge<FireResistant> {
        fun of(): FireResistant {
            return Value
        }

        override fun codec(id: String): ItemComponentType<FireResistant> {
            return Codec(id)
        }

        override fun templateType(): ItemTemplateType<Template> {
            return TemplateType
        }
    }

    private data object Value : FireResistant, ItemComponentMeta {
        override val configPath: String = ItemComponentConstants.FIRE_RESISTANT
        override val tooltipKey: TooltipKey = ItemComponentConstants.createKey { FIRE_RESISTANT }

        override fun provideTooltipLore(): LoreLine {
            if (!config.showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, listOf(tooltip.render()))
        }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
        private val tooltip: ItemComponentConfig.SingleTooltip = config.SingleTooltip()
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<FireResistant> {
        override fun read(holder: ItemComponentHolder): FireResistant? {
            if (!holder.item.itemMeta.isFireResistant) {
                return null
            }
            return Value
        }

        override fun write(holder: ItemComponentHolder, value: FireResistant) {
            holder.item.editMeta { it.isFireResistant = true }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta { it.isFireResistant = false }
        }
    }

    data object Template : ItemTemplate<FireResistant> {
        override val componentType: ItemComponentType<FireResistant> = ItemComponentTypes.FIRE_RESISTANT

        override fun generate(context: GenerationContext): GenerationResult<FireResistant> {
            return GenerationResult.of(Value)
        }
    }

    private data object TemplateType : ItemTemplateType<Template> {
        override val typeToken: TypeToken<Template> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>: {}
         * ```
         */
        override fun deserialize(type: Type, node: ConfigurationNode): Template {
            return Template
        }
    }
}