package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponent
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
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

interface Kizamiable : Examinable, ItemComponent, TooltipProvider.Single {

    companion object : ItemComponentBridge<Kizamiable> {
        fun of(): Kizamiable {
            return Value
        }

        override fun codec(id: String): ItemComponentType<Kizamiable> {
            return Codec(id)
        }

        override fun templateType(): ItemTemplateType<Template> {
            return TemplateType
        }
    }

    private data object Value : Kizamiable, ItemComponentConfig(ItemComponentConstants.KIZAMIZ) {
        private val tooltipKey: TooltipKey = ItemComponentConstants.createKey { KIZAMIZ }
        private val tooltipText: SingleTooltip = SingleTooltip()

        override fun provideTooltipLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, listOf(tooltipText.render()))
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Kizamiable> {
        override fun read(holder: ItemComponentHolder): Kizamiable? {
            return if (holder.hasTag()) Value else null
        }

        override fun write(holder: ItemComponentHolder, value: Kizamiable) {
            holder.putTag()
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }
    }

    data object Template : ItemTemplate<Kizamiable> {
        override val componentType: ItemComponentType<Kizamiable> = ItemComponentTypes.KIZAMIABLE

        override fun generate(context: GenerationContext): GenerationResult<Kizamiable> {
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