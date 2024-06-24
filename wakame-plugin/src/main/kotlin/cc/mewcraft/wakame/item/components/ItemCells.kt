package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.examination.Examinable

interface ItemCells : Examinable {
    class Codec(override val id: String) : ItemComponentType.Valued<ItemCells, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        override fun read(holder: ItemComponentHolder.NBT): ItemCells? {
            TODO("Not yet implemented")
        }

        override fun write(holder: ItemComponentHolder.NBT, value: ItemCells) {
            TODO("Not yet implemented")
        }

        override fun remove(holder: ItemComponentHolder.NBT) {
            TODO("Not yet implemented")
        }
    }
}