package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.examination.Examinable


interface FireResistant : Examinable {

    companion object : ItemComponentBridge<Boolean> {
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

        override fun codec(id: String): ItemComponentType<Boolean> {
            return Codec(id)
        }
    }

    private data object Value : FireResistant

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Boolean> {
        override fun read(holder: ItemComponentHolder): Boolean {
            return !holder.item.itemMeta.isFireResistant
        }

        override fun write(holder: ItemComponentHolder, value: Boolean) {
            holder.item.editMeta { it.isFireResistant = true }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta { it.isFireResistant = false }
        }
    }
}