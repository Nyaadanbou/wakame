package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface Kizamiable : Examinable, TooltipProvider {

    companion object Value : Kizamiable, ItemComponentConfig(ItemComponentConstants.KIZAMIZ) {
        private val tooltipKey: TooltipKey = ItemComponentConstants.createKey { KIZAMIZ }
        private val tooltipText: SingleTooltip = SingleTooltip()

        override fun provideDisplayLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, listOf(tooltipText.render()))
        }
    }

    data class Codec(
        override val id: String,
    ) : ItemComponentType<Kizamiable, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT
        override fun read(holder: ItemComponentHolder.NBT): Kizamiable = Value
        override fun write(holder: ItemComponentHolder.NBT, value: Kizamiable) = Unit
        override fun remove(holder: ItemComponentHolder.NBT) = Unit
    }

    data object Template : ItemTemplate<Kizamiable>, ItemTemplateType<Template> {
        override val typeToken: TypeToken<Template> = typeTokenOf()

        override fun generate(context: GenerationContext): GenerationResult<Kizamiable> {
            return GenerationResult.of(Value)
        }

        /**
         * ## Node structure
         * ```yaml
         * <node>: {}
         * ```
         */
        override fun deserialize(type: Type, node: ConfigurationNode): Template {
            return this
        }
    }
}