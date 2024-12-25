package cc.mewcraft.wakame.item.components

import cc.mewcraft.nbt.StringTag
import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.ListTag
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.bukkit.potion.PotionEffect


data class FoodProperties(
    val nutrition: Int,
    val saturation: Float,
    val canAlwaysEat: Boolean,
    val eatSeconds: Float,
    val effects: List<FoodEffect>,
    val abilities: List<Key>, // TODO 2024/6/28 等技能系统完全落地后改成对应的“技能”实例
) : Examinable {

    data class FoodEffect(
        val potionEffect: PotionEffect,
        val probability: Float,
    )

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
            val itemMeta = holder.item.itemMeta ?: return null
            if (!itemMeta.hasFood()) return null

            val craftFood = itemMeta.food
            val nutrition = craftFood.nutrition
            val saturation = craftFood.saturation
            val canAlwaysEat = craftFood.canAlwaysEat()
            val eatSeconds = craftFood.eatSeconds
            val effects = craftFood.effects.map { FoodEffect(it.effect, it.probability) }

            val abilities = holder.getTag()?.getList(TAG_ABILITIES, TagType.STRING)?.map { Key((it as StringTag).value()) } ?: return null

            return FoodProperties(
                nutrition = nutrition,
                saturation = saturation,
                canAlwaysEat = canAlwaysEat,
                eatSeconds = eatSeconds,
                effects = effects,
                abilities = abilities
            )
        }

        override fun write(holder: ItemComponentHolder, value: FoodProperties) {
            holder.item.editMeta { itemMeta ->
                val craftFood = itemMeta.food
                craftFood.nutrition = value.nutrition
                craftFood.saturation = value.saturation
                craftFood.setCanAlwaysEat(value.canAlwaysEat)
                craftFood.eatSeconds = value.eatSeconds
                value.effects.forEach { craftFood.addEffect(it.potionEffect, it.probability) }
                itemMeta.setFood(craftFood)
            }

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
            holder.item.editMeta { itemMeta ->
                itemMeta.setFood(null)
            }

            holder.removeTag()
        }

        private companion object {
            const val TAG_ABILITIES = "ability"
        }
    }
}

