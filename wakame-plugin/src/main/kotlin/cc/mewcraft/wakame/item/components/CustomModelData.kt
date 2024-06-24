package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.backingCustomModelData
import net.kyori.examination.Examinable

interface CustomModelData : Examinable {

    class Codec(override val id: String) : ItemComponentType.Valued<Int, ItemComponentHolder.Item> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.ITEM

        override fun read(holder: ItemComponentHolder.Item): Int? {
            return holder.item.backingCustomModelData
        }

        override fun write(holder: ItemComponentHolder.Item, value: Int) {
            holder.item.backingCustomModelData = value
        }

        override fun remove(holder: ItemComponentHolder.Item) {
            holder.item.backingCustomModelData = null
        }
    }
}