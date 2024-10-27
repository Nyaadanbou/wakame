package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.damage
import cc.mewcraft.wakame.util.isDamageable
import net.kyori.examination.Examinable


interface ItemDamage : Examinable {

    companion object : ItemComponentBridge<Int> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.DAMAGE)

        override fun codec(id: String): ItemComponentType<Int> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Int> {
        override fun read(holder: ItemComponentHolder): Int? {
            val item = holder.item
            if (item.isDamageable) {
                return item.damage
            } else {
                return null
            }
        }

        override fun write(holder: ItemComponentHolder, value: Int) {
            val item = holder.item
            if (item.isDamageable) {
                item.damage = value
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            // 移除 `damage` 物品组件, 相当于将物品损耗降低至 0.
            val item = holder.item
            if (item.isDamageable) {
                item.damage = 0
            }
        }
    }
}