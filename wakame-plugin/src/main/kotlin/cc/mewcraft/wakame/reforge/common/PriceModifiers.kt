package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.util.compileFunc
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import team.unnamed.mocha.MochaEngine
import team.unnamed.mocha.runtime.compiled.MochaCompiledFunction
import team.unnamed.mocha.runtime.compiled.Named

@ConfigSerializable
data class DamagePriceModifier(
    override val expression: String,
    override val operation: PriceModifier.Operation,
) : PriceModifier {
    companion object Shared {
        const val NAME = "damage"
    }

    override val name: String
        get() = NAME

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
    override val expression: String,
    override val operation: PriceModifier.Operation,
) : PriceModifier {
    companion object Shared {
        const val NAME = "value"
    }

    override val name: String
        get() = NAME

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

    override val name: String
        get() = NAME

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
    override val expression: String,
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
    override val expression: String,
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
    override val expression: String,
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