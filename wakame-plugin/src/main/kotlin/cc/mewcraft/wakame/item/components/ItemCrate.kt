package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable


data class ItemCrate(
    /**
     * 盲盒的唯一标识.
     */
    val key: Key,
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
            val tag = holder.getTag() ?: return null
            val key = Key(tag.getString(TAG_KEY))
            return ItemCrate(key = key)
        }

        override fun write(holder: ItemComponentHolder, value: ItemCrate) {
            holder.editTag { tag ->
                tag.putString(TAG_KEY, value.key.asString())
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        private companion object {
            const val TAG_KEY = "key"
        }
    }
}