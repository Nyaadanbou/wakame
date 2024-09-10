package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.util.toStableShort
import net.kyori.examination.Examinable


data class ItemLevel(
    /**
     * 物品的等级.
     */
    val level: Int,
) : Examinable {

    companion object : ItemComponentBridge<ItemLevel> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.LEVEL)

        override fun codec(id: String): ItemComponentType<ItemLevel> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemLevel> {
        override fun read(holder: ItemComponentHolder): ItemLevel? {
            val tag = holder.getTag() ?: return null
            val raw = tag.getInt(TAG_VALUE)
            return ItemLevel(level = raw)
        }

        override fun write(holder: ItemComponentHolder, value: ItemLevel) {
            holder.editTag { tag ->
                val raw = value.level.toStableShort()
                tag.putShort(TAG_VALUE, raw)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object {
            const val TAG_VALUE = "raw"
        }
    }
}
