package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.util.backingCustomModelData
import net.kyori.examination.Examinable


data class CustomModelData(
    val data: Int,
) : Examinable {

    companion object : ItemComponentBridge<CustomModelData> {
        /**
         * 该组件的配置文件.
         */
        private val config = ItemComponentConfig.provide(ItemConstants.CUSTOM_MODEL_DATA)

        override fun codec(id: String): ItemComponentType<CustomModelData> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<CustomModelData> {
        override fun read(holder: ItemComponentHolder): CustomModelData? {
            return holder.item.backingCustomModelData?.let { CustomModelData(it) }
        }

        override fun write(holder: ItemComponentHolder, value: CustomModelData) {
            holder.item.backingCustomModelData = value.data
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.backingCustomModelData = null
        }
    }
}