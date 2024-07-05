package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponent
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.ItemTemplateType
import net.kyori.examination.Examinable

// TODO 完成组件: ItemTracks

interface ItemTracks : Examinable, ItemComponent {

    companion object : ItemComponentBridge<ItemTracks>, ItemComponentConfig(ItemComponentConstants.TRACKABLE) {
        override fun codec(id: String): ItemComponentType<ItemTracks> {
            return Codec(id)
        }

        override fun templateType(): ItemTemplateType<Nothing> {
            throw UnsupportedOperationException()
        }
    }

    private data class Value(
        val map: Map<String, Int>,
    ) : ItemTracks

    private data class Codec(
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
}