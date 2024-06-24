package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable

interface CustomName : Examinable {

    class Codec(
        override val id: String,
    ) : ItemComponentType<Component, ItemComponentHolder.Item> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.ITEM

        override fun read(holder: ItemComponentHolder.Item): Component? {
            return holder.item.itemMeta.displayName()
        }

        override fun write(holder: ItemComponentHolder.Item, value: Component) {
            holder.item.editMeta { it.displayName(value) }
        }

        override fun remove(holder: ItemComponentHolder.Item) {
            holder.item.editMeta { it.displayName(null) }
        }
    }
}
