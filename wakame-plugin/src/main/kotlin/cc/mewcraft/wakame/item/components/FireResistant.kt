package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.GenerationContext
import cc.mewcraft.wakame.item.component.GenerationResult
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.ItemTemplate
import net.kyori.examination.Examinable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface FireResistant : Examinable, TooltipProvider {

    companion object Value : FireResistant, ItemComponentConfig(ItemComponentConstants.FIRE_RESISTANT) {
        val tooltipKey: TooltipKey = ItemComponentConstants.createKey { FIRE_RESISTANT }
        val tooltipText: SingleTooltip = SingleTooltip()

        override fun provideDisplayLore(): LoreLine {
            return LoreLine.simple(tooltipKey, listOf(tooltipText.render()))
        }
    }

    class Codec(
        override val id: String,
    ) : ItemComponentType<FireResistant, ItemComponentHolder.Item> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.ITEM

        override fun read(holder: ItemComponentHolder.Item): FireResistant? {
            // TODO 等待 DataComponent API 写个更好的实现
            if (!holder.item.itemMeta.isFireResistant) {
                return null
            }
            return FireResistant
        }

        override fun write(holder: ItemComponentHolder.Item, value: FireResistant) {
            // TODO 等待 DataComponent API 写个更好的实现
            holder.item.editMeta { it.isFireResistant = true }
        }

        override fun remove(holder: ItemComponentHolder.Item) {
            // TODO 等待 DataComponent API 写个更好的实现
            holder.item.editMeta { it.isFireResistant = false }
        }
    }

    object Template : ItemTemplate<FireResistant>, ItemTemplate.Serializer<Template> {
        override fun generate(context: GenerationContext): GenerationResult<FireResistant> {
            TODO("Not yet implemented")
        }

        override fun deserialize(type: Type, node: ConfigurationNode): Template {
            TODO("Not yet implemented")
        }
    }
}