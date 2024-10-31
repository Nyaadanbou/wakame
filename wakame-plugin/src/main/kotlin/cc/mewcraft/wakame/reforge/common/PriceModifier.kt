package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.util.compileFunc
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import team.unnamed.mocha.MochaEngine
import team.unnamed.mocha.runtime.compiled.MochaCompiledFunction
import team.unnamed.mocha.runtime.compiled.Named

interface PriceModifier {
    val name: String
    val expression: String
    val operation: Operation

    fun evaluate(item: ItemStack): Double

    enum class Operation {
        ADD_VALUE,
        ADD_MULTIPLIED_BASE,
        ADD_MULTIPLIED_TOTAL,
        ;

        fun byName(name: String): Operation? {
            return when (name) {
                "add_value" -> ADD_VALUE
                "add_multiplied_base" -> ADD_MULTIPLIED_BASE
                "add_multiplied_total" -> ADD_MULTIPLIED_TOTAL
                else -> null
            }
        }
    }
}

@ConfigSerializable
data class DamagePriceModifier(
    @Transient
    override val name: String = NAME,
    @Required
    override val expression: String,
    @Required
    override val operation: PriceModifier.Operation,
) : PriceModifier {
    companion object Shared {
        const val NAME = "damage"
    }

    private val mocha = MochaEngine.createStandard()
    private val function = mocha.compileFunc<DamageFunction>(expression)

    override fun evaluate(item: ItemStack): Double {
        val nekoStack = item.shadowNeko() ?: return .0
        val damage = nekoStack.damage
        return function.evaluate(damage)
    }

    private interface DamageFunction : MochaCompiledFunction {
        fun evaluate(@Named("value") value: Int): Double
    }
}

@ConfigSerializable
data class LevelPriceModifier(
    @Transient
    override val name: String = NAME,
    @Required
    override val expression: String,
    @Required
    override val operation: PriceModifier.Operation,
) : PriceModifier {
    companion object Shared {
        const val NAME = "value"
    }

    private val mocha = MochaEngine.createStandard()
    private val function = mocha.compileFunc<LevelFunction>(expression)

    override fun evaluate(item: ItemStack): Double {
        val nekoStack = item.shadowNeko() ?: return .0
        val level = nekoStack.level
        return function.evaluate(level)
    }

    private interface LevelFunction : MochaCompiledFunction {
        fun evaluate(@Named("value") value: Int): Double
    }
}

@ConfigSerializable
data class RarityPriceModifier(
    @Transient
    override val name: String = NAME,
    @Required
    override val expression: String,
    @Required
    override val operation: PriceModifier.Operation,
    @Required
    private val mapping: Map<String, Double>,
) : PriceModifier {
    companion object Shared {
        const val NAME = "rarity"
    }

    private val mocha = MochaEngine.createStandard()
    private val function = mocha.compileFunc<RarityFunction>(expression)

    override fun evaluate(item: ItemStack): Double {
        val nekoStack = item.shadowNeko() ?: return .0
        val rarity = nekoStack.rarity
        val mapped = mapping[rarity.uniqueId] ?: return .0
        return function.evaluate(mapped)
    }

    private interface RarityFunction : MochaCompiledFunction {
        fun evaluate(@Named("value") value: Double): Double
    }
}

@ConfigSerializable
data class MergingPenaltyPriceModifier(
    @Required
    override val expression: String,
    @Required
    override val operation: PriceModifier.Operation,
) : PriceModifier {
    companion object Shared {
        const val NAME = "merge_penalty"
    }

    override val name: String
        get() = NAME

    override fun evaluate(item: ItemStack): Double {
        // TODO #227
        return .0
    }

    private interface MergingPenaltyFunction : MochaCompiledFunction {
        fun evaluate(@Named("value") value: Int): Double
    }
}

@ConfigSerializable
data class ModdingPenaltyPriceModifier(
    @Required
    override val expression: String,
    @Required
    override val operation: PriceModifier.Operation,
) : PriceModifier {
    companion object Shared {
        const val NAME = "mod_penalty"
    }

    override val name: String
        get() = NAME

    override fun evaluate(item: ItemStack): Double {
        // TODO #227
        return .0
    }

    private interface ModdingPenaltyFunction : MochaCompiledFunction {
        fun evaluate(@Named("value") value: Int): Double
    }
}

@ConfigSerializable
data class RerollingPenaltyPriceModifier(
    @Required
    override val expression: String,
    @Required
    override val operation: PriceModifier.Operation,
) : PriceModifier {
    companion object Shared {
        const val NAME = "reroll_penalty"
    }

    override val name: String
        get() = NAME

    override fun evaluate(item: ItemStack): Double {
        // TODO #227
        return .0
    }

    private interface RerollingPenaltyFunction : MochaCompiledFunction {
        fun evaluate(@Named("value") value: Int): Double
    }
}