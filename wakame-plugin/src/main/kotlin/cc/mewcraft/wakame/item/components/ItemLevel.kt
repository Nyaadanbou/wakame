package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.toStableShort
import net.kyori.examination.Examinable
import xyz.xenondevs.commons.provider.immutable.orElse
import xyz.xenondevs.commons.provider.immutable.require


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

        /**
         * 最小的物品等级.
         */
        val minimumLevel: Int by config.provider.entry<Int>("minimum_level").orElse(1).require({ it >= 0 }, { "minimum_level must be greater than or equal to 0" })

        override fun codec(id: String): ItemComponentType<ItemLevel> {
            return Codec(id)
        }
    }

    init {
        require(level > 0) { "level must be a positive integer" }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemLevel> {
        override fun read(holder: ItemComponentHolder): ItemLevel? {
            val tag = holder.getTag() ?: return null
            val raw = tag.getInt(TAG_VALUE)
                .coerceAtLeast(minimumLevel)
            return ItemLevel(level = raw)
        }

        override fun write(holder: ItemComponentHolder, value: ItemLevel) {
            holder.editTag { tag ->
                val raw = value.level
                    .coerceAtLeast(minimumLevel)
                    .toStableShort()
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
