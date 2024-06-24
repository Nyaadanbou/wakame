package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.examination.Examinable

interface Unbreakable : Examinable {

    class Codec(override val id: String) : ItemComponentType.Valued<Unbreakable, ItemComponentHolder.Item> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.ITEM

        override fun read(holder: ItemComponentHolder.Item): Unbreakable? {
            TODO("Not yet implemented")
        }

        override fun write(holder: ItemComponentHolder.Item, value: Unbreakable) {
            TODO("Not yet implemented")
        }

        override fun remove(holder: ItemComponentHolder.Item) {
            TODO("Not yet implemented")
        }
    }
}