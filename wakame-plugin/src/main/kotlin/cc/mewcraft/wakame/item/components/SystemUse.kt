package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import net.kyori.examination.Examinable

interface SystemUse : Examinable {
    companion object : ItemComponentBridge<Unit> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.SYSTEM_USE)

        override fun codec(id: String): ItemComponentType<Unit> {
            return Codec(id)
        }
    }

    // 开发日记 2024/6/27
    // SystemUse 组件只用于内部代码,
    // 因此只有一个 Codec.
    // 它既不需要一个特定的 Value, 因为它只有存在与否;
    // 它也不需要一个特定的 Template, 因为配置文件暂时没有用处.

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Unit> {
        override fun read(holder: ItemComponentHolder): Unit? {
            return if (holder.hasTag()) Unit else null
        }

        override fun write(holder: ItemComponentHolder, value: Unit) {
            holder.editTag()
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }
    }
}