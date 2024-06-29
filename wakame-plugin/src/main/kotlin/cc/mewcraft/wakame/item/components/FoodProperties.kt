package cc.mewcraft.wakame.item.components

import cc.mewcraft.nbt.StringTag
import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.ListTag
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.bukkit.potion.PotionEffect
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

interface FoodProperties : Examinable {

    val nutrition: Int
    val saturation: Float
    val canAlwaysEat: Boolean
    val eatSeconds: Float
    val effects: List<FoodEffect>
    val skills: List<Key> // TODO 2024/6/28 等技能系统完全落地后改成对应的“技能”实例

    data class FoodEffect(
        val potionEffect: PotionEffect,
        val probability: Float,
    )

    data class Value(
        override val nutrition: Int,
        override val saturation: Float,
        override val canAlwaysEat: Boolean,
        override val eatSeconds: Float,
        override val effects: List<FoodEffect>,
        override val skills: List<Key>,
    ) : FoodProperties

    data class Codec(
        override val id: String,
    ) : ItemComponentType<FoodProperties, ItemComponentHolder.Complex> {
        override val holder: ItemComponentType.Holder = ItemComponentType.Holder.COMPLEX

        override fun read(holder: ItemComponentHolder.Complex): FoodProperties? {
            // 目前的逻辑: 如果物品上没有 `minecraft:food` 组件,
            // 那么就算 NBT 里有 `skills` 列表, 依然直接返回 null
            val itemMeta = holder.item.itemMeta ?: return null
            if (!itemMeta.hasFood()) return null

            val craftFood = itemMeta.food
            val nutrition = craftFood.nutrition
            val saturation = craftFood.saturation
            val canAlwaysEat = craftFood.canAlwaysEat()
            val eatSeconds = craftFood.eatSeconds
            val effects = craftFood.effects.map { FoodEffect(it.effect, it.probability) }

            val skills = holder.tag.getList(TAG_SKILLS, TagType.STRING).map { Key((it as StringTag).value()) }

            return Value(
                nutrition = nutrition,
                saturation = saturation,
                canAlwaysEat = canAlwaysEat,
                eatSeconds = eatSeconds,
                effects = effects,
                skills = skills
            )
        }

        override fun write(holder: ItemComponentHolder.Complex, value: FoodProperties) {
            holder.item.editMeta { itemMeta ->
                val craftFood = itemMeta.food
                craftFood.nutrition = value.nutrition
                craftFood.saturation = value.saturation
                craftFood.setCanAlwaysEat(value.canAlwaysEat)
                craftFood.eatSeconds = value.eatSeconds
                value.effects.forEach { craftFood.addEffect(it.potionEffect, it.probability) }
                itemMeta.setFood(craftFood)
            }

            val stringListTag = ListTag { value.skills.map { StringTag.valueOf(it.asString()) }.forEach(::add) }
            holder.tag.put(TAG_SKILLS, stringListTag)
        }

        override fun remove(holder: ItemComponentHolder.Complex) {
            holder.item.editMeta { itemMeta ->
                itemMeta.setFood(null)
            }

            // no-op for NBT part
        }

        private companion object {
            const val TAG_SKILLS = "skills"
        }
    }

    data class Template(
        val nutrition: Int,
        val saturation: Float,
        val canAlwaysEat: Boolean,
        val eatSeconds: Float,
        val effects: List<FoodEffect>,
        val skills: List<Key>,
    ) : ItemTemplate<FoodProperties> {
        override fun generate(context: GenerationContext): GenerationResult<FoodProperties> {
            val foodProperties = Value(
                nutrition,
                saturation,
                canAlwaysEat,
                eatSeconds,
                effects,
                skills
            )
            return GenerationResult.of(foodProperties)
        }

        companion object : ItemTemplateType<Template> {
            override val typeToken: TypeToken<Template> = typeTokenOf()

            /**
             * ## Node structure
             * ```yaml
             * <node>:
             *   nutrition: <int>
             *   saturation: <float>
             *   can_always_eat: <boolean>
             *   eat_seconds: <float>
             *   effects:
             *     - probability: <float>
             *       effect: <potion_effect>
             * ```
             */
            override fun deserialize(type: Type, node: ConfigurationNode): Template {
                val nutrition = node.node("nutrition").getInt(0)
                val saturation = node.node("saturation").getFloat(0F)
                val canAlwaysEat = node.node("can_always_eat").getBoolean(false)
                val eatSeconds = node.node("eat_seconds").getFloat(1.6F)
                val effects = node.node("effects").childrenList().map { child ->
                    val probability = child.node("probability").getFloat(1F).takeIf { it > 0F && it <= 1F } ?: throw SerializationException(child, javaTypeOf<Float>(), "The probability of a single food effect must between 0 (exclusive) and 1 (inclusive)")
                    val potionEffect = child.node("effect").get<PotionEffect>() ?: throw SerializationException(child, javaTypeOf<PotionEffect>(), "The potion effect of a single food effect must be specifically set")
                    FoodEffect(potionEffect, probability)
                }
                val skills = node.node("skills").getList<Key>(emptyList())
                return Template(nutrition, saturation, canAlwaysEat, eatSeconds, effects, skills)
            }
        }
    }
}

