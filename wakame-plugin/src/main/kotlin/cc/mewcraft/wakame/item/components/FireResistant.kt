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

interface FireResistant : Examinable, TooltipProvider {

    companion object Value : FireResistant, ItemComponentConfig(ItemComponentConstants.FIRE_RESISTANT) {
        private val tooltipKey: TooltipKey = ItemComponentConstants.createKey { FIRE_RESISTANT }
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
    ) : ItemComponentType<FireResistant, ItemComponentHolder.Item> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.ITEM

        override fun read(holder: ItemComponentHolder.Item): FireResistant? {
            if (!holder.item.itemMeta.isFireResistant) {
                return null
            }
            return FireResistant
        }

        override fun write(holder: ItemComponentHolder.Item, value: FireResistant) {
            holder.item.editMeta { it.isFireResistant = true }
        }

        override fun remove(holder: ItemComponentHolder.Item) {
            holder.item.editMeta { it.isFireResistant = false }
        }
    }

    data object Template : ItemTemplate<FireResistant>, ItemTemplateType<Template> {
        override fun generate(context: GenerationContext): GenerationResult<FireResistant> {
            return GenerationResult.of(FireResistant)
        }

        override fun deserialize(type: Type, node: ConfigurationNode): Template {
            return this
        }
    }
}