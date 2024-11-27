package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.itemName
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent


interface ItemName {
    companion object : ItemComponentBridge<Component>, KoinComponent {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.ITEM_NAME)

        override fun codec(id: String): ItemComponentType<Component> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Component> {
        override fun read(holder: ItemComponentHolder): Component? {
            // 2024/6/29
            // 设计上 custom_name 和 item_name 都不经过发包系统处理,
            // 因此这里有什么就读取什么. 整体上做到简单, 一致, 无例外.
            return holder.item.itemName
        }

        override fun write(holder: ItemComponentHolder, value: Component) {
            // 2024/6/29
            // 设计上 custom_name 和 item_name 都不经过发包系统处理,
            // 因此这里有什么就写入什么. 整体上做到简单, 一致, 无例外.
            holder.item.itemName = value
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.itemName = null
        }
    }
}