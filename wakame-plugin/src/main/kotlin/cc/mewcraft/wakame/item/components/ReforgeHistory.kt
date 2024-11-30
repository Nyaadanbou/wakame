package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType

/**
 * @property modCount 已修改的次数
 */
data class ReforgeHistory(
    val modCount: Int,
) {
    companion object : ItemComponentBridge<ReforgeHistory> {
        /**
         * 该组件的配置文件.
         */
        private val config = ItemComponentConfig.provide(ItemConstants.REFORGE_HISTORY)

        /**
         * 一个崭新的 [ReforgeHistory], 相当于从未重铸过.
         */
        @JvmStatic
        val ZERO: ReforgeHistory = ReforgeHistory(0)

        override fun codec(id: String): ItemComponentType<ReforgeHistory> {
            return Codec(id)
        }
    }

    fun incCount(value: Int): ReforgeHistory {
        return copy(modCount = modCount + value)
    }

    fun decCount(value: Int): ReforgeHistory {
        return copy(modCount = modCount - value)
    }

    private data class Codec(override val id: String) : ItemComponentType<ReforgeHistory> {
        companion object {
            private const val TAG_MOD_COUNT = "mod_count"
        }

        override fun read(holder: ItemComponentHolder): ReforgeHistory? {
            val tag = holder.getTag() ?: return null
            val modCount = tag.getInt(TAG_MOD_COUNT)
            return ReforgeHistory(modCount)
        }

        override fun write(holder: ItemComponentHolder, value: ReforgeHistory) {
            holder.editTag { tag ->
                tag.putInt(TAG_MOD_COUNT, value.modCount)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }
    }
}
