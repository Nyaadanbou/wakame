package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.examination.Examinable


data class ItemCrate(
    /**
     * 盲盒的唯一标识.
     */
    val identity: String,
) : Examinable {

    companion object : ItemComponentBridge<ItemCrate> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.CRATE)

        override fun codec(id: String): ItemComponentType<ItemCrate> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemCrate> {
        override fun read(holder: ItemComponentHolder): ItemCrate? {
            val tag = holder.getNbt() ?: return null
            val key = tag.getString(TAG_ID)
            return ItemCrate(identity = key)
        }

        override fun write(holder: ItemComponentHolder, value: ItemCrate) {
            holder.editNbt { tag ->
                tag.putString(TAG_ID, value.identity)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeNbt()
        }

        private companion object {
            const val TAG_ID = "id"
        }
    }
}