package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.backingCustomModelData
import net.kyori.examination.Examinable

interface CustomModelData : Examinable {

    // 开发日记: 2024/6/25
    // 这里的 Codec 的 T 没有定义为 CustomModelData, 而直接就是个 Int
    // 这也意味着我们不需要为其写专门的 class Value 和 class Template.

    data class Codec(
        override val id: String,
    ) : ItemComponentType<Int> {
        override fun read(holder: ItemComponentHolder): Int? {
            return holder.item.backingCustomModelData
        }

        override fun write(holder: ItemComponentHolder, value: Int) {
            holder.item.backingCustomModelData = value
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.backingCustomModelData = null
        }
    }
}