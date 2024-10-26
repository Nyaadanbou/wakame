package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.util.customName0
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent


// 开发日记 2024/6/28
// CustomName 有几个需要思考的问题:
// 1. 在物品上存什么;
// 2. 从物品上获取时返回什么;
// 3. 在后台模板上存什么;

// 开发日记 2024/10/19
// CustomName 就直接读取原版物品组件 `minecraft:custom_name` 的值吧

interface CustomName {
    companion object : ItemComponentBridge<Component>, KoinComponent {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.CUSTOM_NAME)

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
            return holder.item.customName0
        }

        override fun write(holder: ItemComponentHolder, value: Component) {
            // 2024/6/29
            // 设计上 custom_name 和 item_name 都不经过发包系统处理,
            // 因此这里有什么就写入什么. 整体上做到简单, 一致, 无例外.
            holder.item.customName0 = value
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.customName0 = null
        }
    }
}
