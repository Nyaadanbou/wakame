package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.getByteOrNull
import net.kyori.examination.Examinable


data class ItemRarity(
    /**
     * 物品的稀有度.
     */
    val rarity: Rarity,
) : Examinable {

    companion object : ItemComponentBridge<ItemRarity> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.RARITY)

        override fun codec(id: String): ItemComponentType<ItemRarity> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemRarity> {
        override fun read(holder: ItemComponentHolder): ItemRarity? {
            val tag = holder.getTag() ?: return null
            val raw = tag.getByteOrNull(TAG_VALUE)?.let(RarityRegistry::findBy) ?: return null
            return ItemRarity(rarity = raw)
        }

        override fun write(holder: ItemComponentHolder, value: ItemRarity) {
            holder.editTag { tag ->
                tag.putByte(TAG_VALUE, value.rarity.binaryId)
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
