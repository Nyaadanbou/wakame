package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.backingCustomModelData
import net.kyori.examination.Examinable

interface CustomModelData : Examinable {

    // 开发日记: 2024/6/25
    // 这里的 Codec 的 T 没有定义为 CustomModelData, 而直接就是个 Int
    // 这也意味着我们不需要为其写专门的 class Value.
    // FIXME 但问题来了, class Template 需要吗?

    class Codec(
        override val id: String,
    ) : ItemComponentType<Int, ItemComponentHolder.Item> {
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