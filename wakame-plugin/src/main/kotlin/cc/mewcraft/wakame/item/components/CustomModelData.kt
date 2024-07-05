package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponent
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.backingCustomModelData
import net.kyori.examination.Examinable

data class CustomModelData(
    val data: Int,
) : Examinable, ItemComponent {

    companion object : ItemComponentBridge<CustomModelData> {
        override fun codec(id: String): ItemComponentType<CustomModelData> {
            return Codec(id)
        }

        // 开发日记 2024/7/5
        // 设计上 CustomModelData 不能由物品的配置文件决定,
        // 因此这里也没写对应的 Template 实现

        override fun templateType(): ItemTemplateType<ItemTemplate<CustomModelData>> {
            throw UnsupportedOperationException()
        }
    }

    data class Codec(
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