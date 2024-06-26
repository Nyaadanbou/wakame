package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.examination.Examinable

interface SystemUse : Examinable {
    class Codec(
        override val id: String,
    ) : ItemComponentType<Unit, ItemComponentHolder.NBT> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.NBT

        override fun read(holder: ItemComponentHolder.NBT): Unit? = null
        override fun write(holder: ItemComponentHolder.NBT, value: Unit) = Unit
        override fun remove(holder: ItemComponentHolder.NBT) = Unit
    }
}