package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.examination.Examinable

// TODO 完成组件: ItemTracks

interface ItemTracks : Examinable {

    data class Value(
        val map: Map<String, Int>,
    ) : ItemTracks

    data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemTracks> {
        override fun read(holder: ItemComponentHolder): ItemTracks? {
            val tag = holder.getTag() ?: return null
            return Value(emptyMap())
        }

        override fun write(holder: ItemComponentHolder, value: ItemTracks) {
            val tag = holder.getTagOrCreate()
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }
    }

    // 开发日记 2024/7/2 小米
    // ItemTracks 没有必要添加模板,
    // 因为其数据不应该由配置文件指定,
    // 而是由玩家的交互去更新.
    // data object Template
}