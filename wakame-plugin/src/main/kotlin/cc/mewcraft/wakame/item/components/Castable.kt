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

interface Castable : Examinable, TooltipProvider.Single {

    companion object : ItemComponentBridge<Castable>, ItemComponentMeta {
        fun of(): Castable {
            return Value
        }

        override fun codec(id: String): ItemComponentType<Castable> {
            return Codec(id)
        }

        override fun templateType(): ItemTemplateType<Template> {
            return TemplateType
        }

        override val configPath: String = ItemComponentConstants.CASTABLE
        override val tooltipKey: TooltipKey = ItemComponentConstants.createKey { CASTABLE }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
        private val tooltip: ItemComponentConfig.SingleTooltip = config.SingleTooltip()
    }

    private data object Value : Castable {
        override fun provideTooltipLore(): LoreLine {
            if (!config.showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, listOf(tooltip.render()))
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Castable> {
        override fun read(holder: ItemComponentHolder): Castable? {
            return if (holder.hasTag()) Value else null
        }

        override fun write(holder: ItemComponentHolder, value: Castable) {
            holder.putTag()
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }
    }

    data object Template : ItemTemplate<Castable> {
        override val componentType: ItemComponentType<Castable> = ItemComponentTypes.CASTABLE

        override fun generate(context: GenerationContext): GenerationResult<Castable> {
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