package cc.mewcraft.wakame.item.components

import cc.mewcraft.nbt.*
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.util.getListOrNull
import net.kyori.examination.Examinable


data class ExtraLore(
    /**
     * 物品的额外描述.
     */
    val lore: List<String>,
) : Examinable {

    companion object : ItemComponentBridge<ExtraLore> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.LORE)

        override fun codec(id: String): ItemComponentType<ExtraLore> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ExtraLore> {
        override fun read(holder: ItemComponentHolder): ExtraLore? {
            val tag = holder.getTag() ?: return null
            val stringList = tag.getListOrNull(TAG_VALUE, TagType.STRING)?.map { (it as StringTag).value() } ?: return null
            return ExtraLore(lore = stringList)
        }

        override fun write(holder: ItemComponentHolder, value: ExtraLore) {
            holder.editTag { tag ->
                val stringTagList = value.lore.map(StringTag::valueOf)
                val stringListTag = ListTag.create(stringTagList, TagType.STRING)
                tag.put(TAG_VALUE, stringListTag)
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
