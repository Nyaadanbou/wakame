package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.maxDamage
import cc.mewcraft.wakame.util.unsetDamageable
import net.kyori.examination.Examinable


interface ItemMaxDamage : Examinable {
    companion object : ItemComponentBridge<Int> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.MAX_DAMAGE)

        override fun codec(id: String): ItemComponentType<Int> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Int> {
        override fun read(holder: ItemComponentHolder): Int {
            return holder.item.maxDamage
        }

        override fun write(holder: ItemComponentHolder, value: Int) {
            holder.item.maxDamage = value
        }

        override fun remove(holder: ItemComponentHolder) {
            // 移除 `max_damage` 物品组件, 相当于让物品变得不可损耗.
            // TODO 依赖nms直接移除
            val item = holder.item
            item.unsetDamageable()
        }
    }
}