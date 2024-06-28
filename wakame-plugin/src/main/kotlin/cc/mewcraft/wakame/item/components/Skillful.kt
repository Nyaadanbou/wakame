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
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface Skillful : Examinable, TooltipProvider {

    companion object Value : Skillful, ItemComponentConfig(ItemComponentConstants.SKILLFUL) {
        private val tooltipKey: TooltipKey = ItemComponentConstants.createKey { SKILLFUL }
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
    ) : ItemComponentType<Skillful, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT
        override fun read(holder: ItemComponentHolder.NBT): Skillful = Value
        override fun write(holder: ItemComponentHolder.NBT, value: Skillful) = Unit
        override fun remove(holder: ItemComponentHolder.NBT) = Unit
    }

    data object Template : ItemTemplate<Skillful>, ItemTemplateType<Template> {
        override fun generate(context: GenerationContext): GenerationResult<Skillful> {
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