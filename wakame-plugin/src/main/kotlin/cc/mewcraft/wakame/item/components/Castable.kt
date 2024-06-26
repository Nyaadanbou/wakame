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

interface Castable : Examinable, TooltipProvider {

    companion object Value : Castable, ItemComponentConfig(ItemComponentConstants.CASTABLE) {
        private val tooltipKey: TooltipKey = ItemComponentConstants.createKey { CASTABLE }
        private val tooltipText: SingleTooltip = SingleTooltip()

        override fun provideDisplayLore(): LoreLine {
            if (!showInTooltip) {
                return LoreLine.noop()
            }
            return LoreLine.simple(tooltipKey, listOf(tooltipText.render()))
        }
    }

    class Codec(override val id: String) : ItemComponentType<Castable, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT
        override fun read(holder: ItemComponentHolder.NBT): Castable = Castable
        override fun write(holder: ItemComponentHolder.NBT, value: Castable) = Unit
        override fun remove(holder: ItemComponentHolder.NBT) = Unit
    }

    object Template : ItemTemplate<Castable>, ItemTemplateType<Template> {
        override fun generate(context: GenerationContext): GenerationResult<Castable> {
            return GenerationResult.of(Value)
        }

        override fun deserialize(type: Type, node: ConfigurationNode): Template {
            return this
        }
    }
}