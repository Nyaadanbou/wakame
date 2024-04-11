package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.annotation.ConfigPath
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.potion.PotionEffect
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.lang.reflect.Type
import java.util.stream.Stream

data class Food(
    val nutrition: Int = 0,
    val saturationModifier: Float = 0F,
    val isMeat: Boolean = false,
    val canAlwaysEat: Boolean = false,
    val eatSeconds: Float = 1.6F,
    val effects: Map<PotionEffect, Float>,
) : Examinable {
    init {
        require(nutrition >= 0) { "nutrition >= 0" }
        require(saturationModifier >= 0) { "saturationModifier >= 0" }
        require(eatSeconds > 0) { "eatSeconds > 0" }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("nutrition", nutrition),
        ExaminableProperty.of("saturationModifier", saturationModifier),
        ExaminableProperty.of("isMeat", isMeat),
        ExaminableProperty.of("canAlwaysEat", canAlwaysEat),
        ExaminableProperty.of("eatSeconds", eatSeconds),
        ExaminableProperty.of("effects", effects)
    )

    override fun toString(): String = toSimpleString()
}

@ConfigPath(ItemMetaConstants.FOOD)
sealed interface SFoodMeta : SchemaItemMeta<Food> {
    override val key: Key get() = ItemMetaConstants.createKey { FOOD }
}

private class NonNullFoodMeta(
    private val nutrition: Int = 0,
    private val saturationModifier: Float = 0F,
    private val isMeat: Boolean = false,
    private val canAlwaysEat: Boolean = false,
    private val eatSeconds: Float = 1.6F,
    private val effects: Map<PotionEffect, Float>,
) : SFoodMeta {
    override val isEmpty: Boolean = false

    init {
        require(nutrition >= 0) { "nutrition >= 0" }
        require(saturationModifier >= 0) { "saturationModifier >= 0" }
        require(eatSeconds > 0) { "eatSeconds > 0" }
    }

    override fun generate(context: SchemaGenerationContext): GenerationResult<Food> {
        return GenerationResult(Food(nutrition, saturationModifier, isMeat, canAlwaysEat, eatSeconds, effects))
    }
}

private data object DefaultFoodMeta : SFoodMeta {
    override val isEmpty: Boolean = true
    override fun generate(context: SchemaGenerationContext): GenerationResult<Food> = GenerationResult.empty()
}

internal data object FoodMetaSerializer : SchemaItemMetaSerializer<SFoodMeta> {
    override val defaultValue: SFoodMeta = DefaultFoodMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SFoodMeta {
        val nutrition = node.node("nutrition").getInt(0)
        val saturationModifier = node.node("saturation_modifier").getFloat(0F)
        val isMeat = node.node("is_meat").getBoolean(false)
        val canAlwaysEat = node.node("can_always_eat").getBoolean(false)
        val eatSeconds = node.node("eat_seconds").getFloat(1.6F)
        val effects = node.node("effects").get<Map<PotionEffect, Float>>(emptyMap())
        return NonNullFoodMeta(nutrition, saturationModifier, isMeat, canAlwaysEat, eatSeconds, effects)
    }
}