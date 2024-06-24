package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.display.TooltipsProvider
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.examination.Examinable

interface FireResistant : Examinable, TooltipsProvider {

    object Value : FireResistant, ItemComponentConfig(ItemComponentConstants.FIRE_RESISTANT) {
        val key: TooltipKey = ItemComponentConstants.createKey { FIRE_RESISTANT }
        val tooltips: SingleTooltip = SingleTooltip()

        override fun provideDisplayLore(): LoreLine {
            return LoreLine.simple(key, listOf(tooltips.render()))
        }
    }

    class Codec(override val id: String) : ItemComponentType.NonValued<ItemComponentHolder.Item> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.ITEM

        override fun read(holder: ItemComponentHolder.Item): Boolean {
            // TODO 等待 DataComponent API 写个更好的实现
            return holder.item.itemMeta.isFireResistant
        }

        override fun write(holder: ItemComponentHolder.Item, value: Boolean) {
            // TODO 等待 DataComponent API 写个更好的实现
            holder.item.editMeta { it.isFireResistant = value }
        }

        override fun remove(holder: ItemComponentHolder.Item) {
            // TODO 等待 DataComponent API 写个更好的实现
            holder.item.editMeta { it.isFireResistant = false }
        }
    }
}