package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.item.level
import cc.mewcraft.wakame.item.rarity
import cc.mewcraft.wakame.item.reforgeHistory
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.util.bindInstance
import cc.mewcraft.wakame.util.damage
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import team.unnamed.mocha.MochaEngine
import team.unnamed.mocha.runtime.binding.Binding

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

// TODO 优化这个文件里的 Mocha 的用法
//  降低创建 MochaEngine 实例的开销

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

    override fun evaluate(item: ItemStack): Double {
        val damage = item.damage
        val mocha = MochaEngine.createStandard()
        mocha.bindInstance(DamageBinding(damage), "query")
        return mocha.eval(expression)
    }

    @Binding("query")
    class DamageBinding(
        @JvmField @Binding("value")
        val value: Int,
    )
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
        const val NAME = "level"
    }

    override fun evaluate(item: ItemStack): Double {
        val nekoStack = item.shadowNeko() ?: return .0
        val level = nekoStack.level
        val mocha = MochaEngine.createStandard()
        mocha.bindInstance(LevelBinding(level), "query")
        return mocha.eval(expression)
    }

    @Binding("query")
    class LevelBinding(
        @JvmField @Binding("value")
        val value: Int,
    )
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

    override fun evaluate(item: ItemStack): Double {
        val nekoStack = item.shadowNeko() ?: return .0
        val rarity = nekoStack.rarity
        val mapped = mapping[rarity.uniqueId] ?: return .0
        val mocha = MochaEngine.createStandard()
        mocha.bindInstance(RarityBinding(mapped), "query")
        return mocha.eval(expression)
    }

    @Binding("query")
    class RarityBinding(
        @JvmField @Binding("value")
        val value: Double,
    )
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

    override fun evaluate(item: ItemStack): Double {
        val nekoStack = item.shadowNeko() ?: return .0
        val value = nekoStack.reforgeHistory.modCount
        val mocha = MochaEngine.createStandard()
        mocha.bindInstance(MergingBinding(value), "query")
        return mocha.eval(expression)
    }

    @Binding("query")
    class MergingBinding(
        @JvmField @Binding("value")
        val value: Int,
    )
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

    override fun evaluate(item: ItemStack): Double {
        val nekoStack = item.shadowNeko() ?: return .0
        val value = nekoStack.reforgeHistory.modCount
        val mocha = MochaEngine.createStandard()
        mocha.bindInstance(ModdingBinding(value), "query")
        return mocha.eval(expression)
    }

    @Binding("query")
    class ModdingBinding(
        @JvmField @Binding("value")
        val value: Int,
    )
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

    override fun evaluate(item: ItemStack): Double {
        val nekoStack = item.shadowNeko() ?: return .0
        val value = nekoStack.reforgeHistory.modCount
        val mocha = MochaEngine.createStandard()
        mocha.bindInstance(RerollingBinding(value), "query")
        return mocha.eval(expression)
    }

    @Binding("query")
    class RerollingBinding(
        @JvmField @Binding("value")
        val value: Int,
    )
}