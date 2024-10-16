package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.util.customModelData0
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
            return holder.item.customModelData0?.let { CustomModelData(it) }
        }

        override fun write(holder: ItemComponentHolder, value: CustomModelData) {
            holder.item.customModelData0 = value.data
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.customModelData0 = null
        }
    }
}