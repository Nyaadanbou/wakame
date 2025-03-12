package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.ItemDeprecations
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.adventure.toNMSComponent
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponents


// 开发日记 2024/6/28
// CustomName 有几个需要思考的问题:
// 1. 在物品上存什么;
// 2. 从物品上获取时返回什么;
// 3. 在后台模板上存什么;

// 开发日记 2024/10/19
// CustomName 就直接读取原版物品组件 `minecraft:custom_name` 的值吧

interface CustomName {
    companion object : ItemComponentBridge<Component> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.CUSTOM_NAME)

        override fun codec(id: String): ItemComponentType<Component> {
            return Codec(id)
        }
    }

    // 设计上 custom_name 和 item_name 都不经过发包系统处理,
    // 因此这里有什么就读取什么. 整体上做到简单, 一致, 无例外.
    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Component> {
        override fun read(holder: ItemComponentHolder): Component? {
            ItemDeprecations.usePaperOrNms()
        }

        override fun write(holder: ItemComponentHolder, value: Component) {
            holder.mojangStack.set(DataComponents.CUSTOM_NAME, value.toNMSComponent())
        }

        override fun remove(holder: ItemComponentHolder) {
            ItemDeprecations.usePaperOrNms()
        }
    }
}
