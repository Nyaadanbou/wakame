package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import net.kyori.examination.Examinable


interface FireResistant : Examinable {

    companion object : ItemComponentBridge<FireResistant> {
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

        override fun codec(id: String): ItemComponentType<FireResistant> {
            return Codec(id)
        }
    }

    private data object Value : FireResistant

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<FireResistant> {
        override fun read(holder: ItemComponentHolder): FireResistant? {
            if (!holder.item.itemMeta.isFireResistant) {
                return null
            }
            return Value
        }

        override fun write(holder: ItemComponentHolder, value: FireResistant) {
            holder.item.editMeta { it.isFireResistant = true }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta { it.isFireResistant = false }
        }
    }
}