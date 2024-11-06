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

private val MOCHA: MochaEngine<*> = MochaEngine.createStandard()

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

    private val function: DamageFunction = MOCHA.compileFunc(expression)

    override fun evaluate(item: ItemStack): Double {
        val nekoStack = item.shadowNeko() ?: return .0
        val damage = nekoStack.damage
        return function.evaluate(damage)
    }

    interface DamageFunction : MochaCompiledFunction {
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

    private val function: LevelFunction = MOCHA.compileFunc(expression)

    override fun evaluate(item: ItemStack): Double {
        val nekoStack = item.shadowNeko() ?: return .0
        val level = nekoStack.level
        return function.evaluate(level)
    }

    interface LevelFunction : MochaCompiledFunction {
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

    private val function: RarityFunction = MOCHA.compileFunc(expression)

    override fun evaluate(item: ItemStack): Double {
        val nekoStack = item.shadowNeko() ?: return .0
        val rarity = nekoStack.rarity
        val mapped = mapping[rarity.uniqueId] ?: return .0
        return function.evaluate(mapped)
    }

    interface RarityFunction : MochaCompiledFunction {
        fun evaluate(@Named("value") value: Double): Double
    }
}

@ConfigSerializable
data class MergingPenaltyPriceModifier(
    @Transient
    override val name: String = NAME,
    @Required
    override val expression: String,
    @Required
    override val operation: PriceModifier.Operation,
) : PriceModifier {
    companion object Shared {
        const val NAME = "merge_penalty"
    }

    private val function: MergingPenaltyFunction = MOCHA.compileFunc(expression)

    override fun evaluate(item: ItemStack): Double {
        val nekoStack = item.shadowNeko() ?: return .0
        val portableCore = nekoStack.portableCore ?: return .0
        val value = portableCore.penalty
        return function.evaluate(value)
    }

    interface MergingPenaltyFunction : MochaCompiledFunction {
        fun evaluate(@Named("value") value: Int): Double
    }
}

@ConfigSerializable
data class ModdingPenaltyPriceModifier(
    @Transient
    override val name: String = NAME,
    @Required
    override val expression: String,
    @Required
    override val operation: PriceModifier.Operation,
) : PriceModifier {
    companion object Shared {
        const val NAME = "mod_penalty"
    }

    private val function: ModdingPenaltyFunction = MOCHA.compileFunc(expression)

    override fun evaluate(item: ItemStack): Double {
        val nekoStack = item.shadowNeko() ?: return .0
        val cells = nekoStack.cells ?: return .0
        val value = cells.map { it.value }.sumOf { it.getReforgeHistory().modCount }
        return function.evaluate(value)
    }

    interface ModdingPenaltyFunction : MochaCompiledFunction {
        fun evaluate(@Named("value") value: Int): Double
    }
}

@ConfigSerializable
data class RerollingPenaltyPriceModifier(
    @Transient
    override val name: String = NAME,
    @Required
    override val expression: String,
    @Required
    override val operation: PriceModifier.Operation,
) : PriceModifier {
    companion object Shared {
        const val NAME = "reroll_penalty"
    }

    private val function: RerollingPenaltyFunction = MOCHA.compileFunc(expression)

    override fun evaluate(item: ItemStack): Double {
        val nekoStack = item.shadowNeko() ?: return .0
        val cells = nekoStack.cells ?: return .0
        val value = cells.map { it.value }.sumOf { it.getReforgeHistory().rerollCount }
        return function.evaluate(value)
    }

    interface RerollingPenaltyFunction : MochaCompiledFunction {
        fun evaluate(@Named("value") value: Int): Double
    }
}