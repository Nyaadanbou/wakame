package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.config.configurate.PotionEffectSerializer
import cc.mewcraft.wakame.config.configurate.PotionEffectTypeSerializer
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.FoodProperties.FoodEffect
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.*
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.key.Key
import org.bukkit.potion.PotionEffect
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import cc.mewcraft.wakame.item.components.FoodProperties as FoodPropertiesData


data class FoodProperties(
    val nutrition: Int,
    val saturation: Float,
    val canAlwaysEat: Boolean,
    val eatSeconds: Float,
    val effects: List<FoodEffect>,
    val skills: List<Key>,
) : ItemTemplate<FoodPropertiesData> {
    override val componentType: ItemComponentType<FoodPropertiesData> = ItemComponentTypes.FOOD

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<FoodPropertiesData> {
        val foodProperties = FoodPropertiesData(
            nutrition,
            saturation,
            canAlwaysEat,
            eatSeconds,
            effects,
            skills
        )
        return ItemGenerationResult.of(foodProperties)
    }

    companion object : ItemTemplateBridge<FoodProperties> {
        override fun codec(id: String): ItemTemplateType<FoodProperties> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<FoodProperties> {
        override val type: TypeToken<FoodProperties> = typeTokenOf()

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
        override fun decode(node: ConfigurationNode): FoodProperties {
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
            return FoodProperties(nutrition, saturation, canAlwaysEat, eatSeconds, effects, skills)
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .kregister(PotionEffectSerializer)
                .kregister(PotionEffectTypeSerializer)
                .build()
        }
    }
}