package cc.mewcraft.wakame.item.components

import cc.mewcraft.nbt.StringTag
import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.config.configurate.PotionEffectSerializer
import cc.mewcraft.wakame.config.configurate.PotionEffectTypeSerializer
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentMeta
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.ListTag
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import org.bukkit.potion.PotionEffect
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializerCollection

data class FoodProperties(
    val nutrition: Int,
    val saturation: Float,
    val canAlwaysEat: Boolean,
    val eatSeconds: Float,
    val effects: List<FoodEffect>,
    val skills: List<Key>, // TODO 2024/6/28 等技能系统完全落地后改成对应的“技能”实例
) : Examinable {

    data class FoodEffect(
        val potionEffect: PotionEffect,
        val probability: Float,
    )

    companion object : ItemComponentBridge<FoodProperties>, ItemComponentMeta {
        override fun codec(id: String): ItemComponentType<FoodProperties> {
            return Codec(id)
        }

        override fun templateType(id: String): ItemTemplateType<Template> {
            return TemplateType(id)
        }

        override val configPath: String = ItemComponentConstants.FOOD
        override val tooltipKey: Key = ItemComponentConstants.createKey { FOOD }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<FoodProperties> {
        override fun read(holder: ItemComponentHolder): FoodProperties? {
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

            val skills = holder.getTag()?.getList(TAG_SKILLS, TagType.STRING)?.map { Key((it as StringTag).value()) } ?: return null

            return FoodProperties(
                nutrition = nutrition,
                saturation = saturation,
                canAlwaysEat = canAlwaysEat,
                eatSeconds = eatSeconds,
                effects = effects,
                skills = skills
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
                    value.skills
                        .map { StringTag.valueOf(it.asString()) }
                        .forEach(::add)
                }
                tag.put(TAG_SKILLS, stringListTag)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta { itemMeta ->
                itemMeta.setFood(null)
            }

            holder.removeTag()
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
        override val componentType: ItemComponentType<FoodProperties> = ItemComponentTypes.FOOD

        override fun generate(context: GenerationContext): GenerationResult<FoodProperties> {
            val foodProperties = FoodProperties(
                nutrition,
                saturation,
                canAlwaysEat,
                eatSeconds,
                effects,
                skills
            )
            return GenerationResult.of(foodProperties)
        }
    }

    private data class TemplateType(
        override val id: String,
    ) : ItemTemplateType<Template> {
        override val type: TypeToken<Template> = typeTokenOf()

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
        override fun decode(node: ConfigurationNode): Template {
            val nutrition = node.node("nutrition").getInt(0)
            val saturation = node.node("saturation").getFloat(0f)
            val canAlwaysEat = node.node("can_always_eat").getBoolean(false)
            val eatSeconds = node.node("eat_seconds").getFloat(1.6f)
            val effects = node.node("effects").childrenList().map { child ->
                val probability = child.node("probability").getFloat(1f).takeIf { it > 0f && it <= 1f } ?: throw SerializationException(child, javaTypeOf<Float>(), "The probability of a single food effect must between 0 (exclusive) and 1 (inclusive)")
                val potionEffect = child.get<PotionEffect>() ?: throw SerializationException(child, javaTypeOf<PotionEffect>(), "The potion effect of a single food effect must be specifically set")
                FoodEffect(potionEffect, probability)
            }
            val skills = node.node("skills").getList<Key>(emptyList())
            return Template(nutrition, saturation, canAlwaysEat, eatSeconds, effects, skills)
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .kregister(PotionEffectSerializer)
                .kregister(PotionEffectTypeSerializer)
                .build()
        }
    }
}

