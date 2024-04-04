package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.toStableInt
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import java.util.stream.Stream
import kotlin.math.ceil

data class Durability(
    val threshold: Int,
    val damage: Int = 0,
) : Examinable {
    val damagePercent: Int
        get() = ceil(damage / threshold.toFloat() * 100).toInt()

    init {
        require(threshold > 0) { "threshold > 0" }
        require(threshold > damage) { "threshold > damage" }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("threshold", threshold),
        ExaminableProperty.of("damage", damage)
    )

    override fun toString(): String = toSimpleString()
}

sealed interface SDurabilityMeta : SchemaItemMeta<Durability> {
    override val key: Key get() = ItemMetaKeys.DURABILITY
}

private class NonNullDurabilityMeta(
    private val threshold: RandomizedValue,
    private val damage: RandomizedValue? = null,
) : SDurabilityMeta {
    override val isEmpty: Boolean = false
    init {
        require(threshold.base > 0) { "threshold.base > 0" }
        damage?.run { require(damage.base >= 0) { "damage.base >= 0" } }
        damage?.run { require(damage.base < threshold.base) { "damage.base < threshold.base" } }
    }

    override fun generate(context: SchemaGenerationContext): GenerationResult<Durability> {
        val threshold = threshold.calculate(context.level)
        val damage = damage?.calculate(context.level) ?: 0 // if damage is null, simply generate 0
        return GenerationResult(Durability(threshold.toStableInt(), damage.toStableInt()))
    }
}

private data object DefaultDurabilityMeta : SDurabilityMeta {
    override val isEmpty: Boolean = true
    override fun generate(context: SchemaGenerationContext): GenerationResult<Durability> = GenerationResult.empty()
}

internal data object DurabilityMetaSerializer : SchemaItemMetaSerializer<SDurabilityMeta> {
    override val defaultValue: SDurabilityMeta = DefaultDurabilityMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SDurabilityMeta {
        val threshold = node.node("threshold").krequire<RandomizedValue>()
        val damage = node.node("damage").takeUnless { it.virtual() }?.krequire<RandomizedValue>() // nullable
        return NonNullDurabilityMeta(threshold, damage)
    }
}