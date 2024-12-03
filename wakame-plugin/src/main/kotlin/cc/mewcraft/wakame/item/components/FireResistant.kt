package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.MojangUnit
import cc.mewcraft.wakame.util.toKotlin
import cc.mewcraft.wakame.util.unwrap
import net.kyori.examination.Examinable
import net.minecraft.core.component.DataComponents


interface FireResistant : Examinable {

    companion object : ItemComponentBridge<Unit> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.FIRE_RESISTANT)

        /**
         * 返回 [FireResistant] 的实例.
         */
        fun instance(): FireResistant {
            return Value
        }

        override fun codec(id: String): ItemComponentType<Unit> {
            return Codec(id)
        }
    }

    private data object Value : FireResistant

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Unit> {

        // 2024/12/3 开发日记 小米
        // 先试试使用 NMS 来读取物品信息

        override fun read(holder: ItemComponentHolder): Unit? {
            return holder.item.unwrap?.get(DataComponents.FIRE_RESISTANT)?.toKotlin
        }

        override fun write(holder: ItemComponentHolder, value: Unit) {
            holder.item.unwrap?.set(DataComponents.FIRE_RESISTANT, MojangUnit.INSTANCE)
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.unwrap?.remove(DataComponents.FIRE_RESISTANT)
        }
    }
}