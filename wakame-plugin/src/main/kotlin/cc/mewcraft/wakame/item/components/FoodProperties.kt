@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.util.data.NbtUtils
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
            val craftFood = holder.bukkitStack.getData(DataComponentTypes.FOOD) ?: return null

            val nutrition = craftFood.nutrition()
            val saturation = craftFood.saturation()
            val canAlwaysEat = craftFood.canAlwaysEat()

            val abilities = holder.getNbt()?.getList(TAG_ABILITIES, NbtUtils.TAG_STRING)?.map { Key.key(it.toString()) } ?: return null

            return FoodProperties(
                nutrition = nutrition,
                saturation = saturation,
                canAlwaysEat = canAlwaysEat,
                abilities = abilities
            )
        }

        override fun write(holder: ItemComponentHolder, value: FoodProperties) {
            holder.bukkitStack.setData(
                DataComponentTypes.FOOD,
                PaperFoodProperties.food()
                    .nutrition(value.nutrition)
                    .saturation(value.saturation)
                    .canAlwaysEat(value.canAlwaysEat)
                    .build()
            )

            holder.editNbt { tag ->
                val abilityIds = value.abilities.map { it.asString() }
                tag.put(TAG_ABILITIES, NbtUtils.stringListTag(abilityIds))
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.bukkitStack.unsetData(DataComponentTypes.FOOD)
            holder.removeNbt()
        }

        private companion object {
            const val TAG_ABILITIES = "ability"
        }
    }
}

