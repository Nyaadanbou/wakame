@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item.components

import cc.mewcraft.nbt.StringTag
import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.ListTag
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import io.papermc.paper.datacomponent.item.FoodProperties as PaperFoodProperties


data class FoodProperties(
    val nutrition: Int,
    val saturation: Float,
    val canAlwaysEat: Boolean,
    val abilities: List<Key>, // TODO 2024/6/28 等技能系统完全落地后改成对应的“技能”实例
) : Examinable {

    companion object : ItemComponentBridge<FoodProperties> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.FOOD)

        override fun codec(id: String): ItemComponentType<FoodProperties> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<FoodProperties> {
        override fun read(holder: ItemComponentHolder): FoodProperties? {
            // 目前的逻辑: 如果物品上没有 `minecraft:food` 组件,
            // 那么就算 NBT 里有 `abilities` 列表, 依然直接返回 null
            val craftFood = holder.item.getData(DataComponentTypes.FOOD) ?: return null

            val nutrition = craftFood.nutrition()
            val saturation = craftFood.saturation()
            val canAlwaysEat = craftFood.canAlwaysEat()

            val abilities = holder.getTag()?.getList(TAG_ABILITIES, TagType.STRING)?.map { Key((it as StringTag).value()) } ?: return null

            return FoodProperties(
                nutrition = nutrition,
                saturation = saturation,
                canAlwaysEat = canAlwaysEat,
                abilities = abilities
            )
        }

        override fun write(holder: ItemComponentHolder, value: FoodProperties) {
            holder.item.setData(
                DataComponentTypes.FOOD,
                PaperFoodProperties.food()
                    .nutrition(value.nutrition)
                    .saturation(value.saturation)
                    .canAlwaysEat(value.canAlwaysEat)
                    .build()
            )

            holder.editTag { tag ->
                val stringListTag = ListTag {
                    value.abilities
                        .map { StringTag.valueOf(it.asString()) }
                        .forEach(::add)
                }
                tag.put(TAG_ABILITIES, stringListTag)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.unsetData(DataComponentTypes.FOOD)
            holder.removeTag()
        }

        private companion object {
            const val TAG_ABILITIES = "ability"
        }
    }
}

