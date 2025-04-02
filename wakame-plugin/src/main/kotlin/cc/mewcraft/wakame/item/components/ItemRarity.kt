package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.rarity2.Rarity
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.data.getStringOrNull
import net.kyori.examination.Examinable


data class ItemRarity(
    /**
     * 物品的稀有度.
     */
    val rarity: RegistryEntry<Rarity>,
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
            val tag = holder.getNbt() ?: return null
            val raw = tag.getStringOrNull(TAG_VALUE)
                ?.let { KoishRegistries2.RARITY.getEntry(it) } ?: return null
            return ItemRarity(rarity = raw)
        }

        override fun write(holder: ItemComponentHolder, value: ItemRarity) {
            holder.editNbt { tag ->
                tag.putString(TAG_VALUE, value.rarity.getKeyOrThrow().value.toString())
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeNbt()
        }

        private companion object {
            const val TAG_VALUE = "raw"
        }
    }
}
