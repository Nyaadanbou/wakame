package cc.mewcraft.wakame.entity.attribute.source

import cc.mewcraft.lazyconfig.configurate.SimpleSerializer
import cc.mewcraft.lazyconfig.configurate.serializer.DispatchingSerializer
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 值修饰器.
 */
sealed interface NumberModifier {
    fun modify(context: NumberModifierContext): Double

    companion object {
        fun serializer(): SimpleSerializer<NumberModifier> {
            return DispatchingSerializer.createPartial(
                mapOf(
                    "effect_level_linear_bonus" to EffectLevelLinearBonusNumberModifier::class,
                    "effect_level_table_bonus" to EffectLevelTableBonusNumberModifier::class,
                )
            )
        }
    }
}

/**
 * 值修饰器使用的上下文.
 */
data class NumberModifierContext(
    val base: Double,
    val effectLevel: Int = 0,
)

/**
 * 基于状态效果等级的线性值修饰器.
 */
@ConfigSerializable
data class EffectLevelLinearBonusNumberModifier(
    val perLevel: Double,
) : NumberModifier {
    override fun modify(context: NumberModifierContext): Double {
        return context.base + context.effectLevel * perLevel
    }
}

/**
 * 基于状态效果等级的打表值修饰器.
 */
@ConfigSerializable
data class EffectLevelTableBonusNumberModifier(
    val bonus: List<Double>,
    val default: Double = 0.0,
) : NumberModifier {
    override fun modify(context: NumberModifierContext): Double {
        return context.base + (bonus.getOrNull(context.effectLevel - 1) ?: default)
    }
}