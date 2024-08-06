package cc.mewcraft.wakame.reforge.rerolling

import cc.mewcraft.wakame.molang.Evaluable
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

internal object RerollingTableWtf : RerollingTable {
    override val enabled: Boolean = true
    override val title: Component = Component.text("Rerolling Table (Wtf)")
    override val cost: RerollingTable.Cost
        get() = TODO("Not yet implemented")
    override val itemRules: RerollingTable.ItemRuleMap
        get() = TODO("Not yet implemented")

    object Cost : RerollingTable.Cost {
        override val base: Double = 0.0
        override val rarityMapping: Map<Key, Double> = emptyMap<Key, Double>().withDefault { 0.0 }
        override val eachCostFormula: Evaluable<*> = Evaluable.parseNumber(0)
        override val totalCostFormula: Evaluable<*> = Evaluable.parseNumber(0)
    }
}

internal class RerollingTableImpl(
    override val enabled: Boolean,
    override val title: Component,
    override val cost: RerollingTable.Cost,
    override val itemRules: RerollingTable.ItemRuleMap,
) : RerollingTable {
}