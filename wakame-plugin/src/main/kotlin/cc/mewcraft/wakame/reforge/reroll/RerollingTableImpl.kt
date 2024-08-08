package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

internal object WtfRerollingTable : RerollingTable {
    override val identifier: String = "wtf"
    override val enabled: Boolean = true
    override val title: Component = Component.text("Rerolling Table (Cheat ON)")
    override val cost: RerollingTable.Cost = ZeroCost
    override val itemRules: RerollingTable.ItemRuleMap = AnyItemRuleMap

    override fun toString(): String {
        return toSimpleString()
    }

    private data object ZeroCost : RerollingTable.Cost {
        override val base: Double = 0.0
        override val rarityNumberMapping: Map<Key, Double> = emptyMap<Key, Double>().withDefault { 0.0 }
        override val eachFunction: RerollingTable.Cost.EachFunction = ZeroEachFunction
        override val totalFunction: RerollingTable.Cost.TotalFunction = ZeroTotalFunction

        object ZeroEachFunction : RerollingTable.Cost.EachFunction {
            override fun compute(cost: Double, maxReroll: Int, rerollCount: Int): Double {
                return .0
            }
        }

        object ZeroTotalFunction : RerollingTable.Cost.TotalFunction {
            override fun compute(
                base: Double,
                rarity: Double,
                itemLevel: Int,
                allCount: Int,
                selectedCount: Int,
                selectedCostSum: Double,
                unselectedCostSum: Double,
            ): Double {
                return .0
            }
        }
    }

    private data object AnyCellRule : RerollingTable.CellRule {
        override val cost: Double = 0.0
        override val maxReroll: Int = Int.MAX_VALUE
    }

    private data object AnyCellRuleMap : RerollingTable.CellRuleMap {
        override fun get(key: String): RerollingTable.CellRule {
            return AnyCellRule
        }
    }

    private data object AnyItemRule : RerollingTable.ItemRule {
        override val cellRules: RerollingTable.CellRuleMap = AnyCellRuleMap
    }

    private data object AnyItemRuleMap : RerollingTable.ItemRuleMap {
        override fun get(key: Key): RerollingTable.ItemRule {
            return AnyItemRule
        }
    }
}

internal class SimpleRerollingTable(
    override val identifier: String,
    override val enabled: Boolean,
    override val title: Component,
    override val cost: RerollingTable.Cost,
    override val itemRules: RerollingTable.ItemRuleMap,
) : RerollingTable {

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("identifier", identifier),
        ExaminableProperty.of("enabled", enabled),
        ExaminableProperty.of("title", title),
        ExaminableProperty.of("cost", cost),
        ExaminableProperty.of("itemRules", itemRules),
    )

    override fun toString(): String =
        toSimpleString()

    data class Cost(
        override val base: Double,
        override val rarityNumberMapping: Map<Key, Double>,
        override val eachFunction: RerollingTable.Cost.EachFunction,
        override val totalFunction: RerollingTable.Cost.TotalFunction,
    ) : RerollingTable.Cost {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("base", base),
            ExaminableProperty.of("rarityNumberMapping", rarityNumberMapping),
            ExaminableProperty.of("eachFunction", eachFunction),
            ExaminableProperty.of("totalFunction", totalFunction),
        )

        override fun toString(): String =
            toSimpleString()
    }

    data class CellRule(
        override val cost: Double,
        override val maxReroll: Int,
    ) : RerollingTable.CellRule {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("cost", cost),
            ExaminableProperty.of("modLimit", maxReroll),
        )

        override fun toString(): String =
            toSimpleString()
    }

    data class CellRuleMap(
        private val map: Map<String, RerollingTable.CellRule>,
    ) : RerollingTable.CellRuleMap {
        override fun get(key: String): RerollingTable.CellRule? {
            return map[key]
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("map", map),
        )

        override fun toString(): String =
            toSimpleString()
    }

    data class ItemRule(
        override val cellRules: RerollingTable.CellRuleMap,
    ) : RerollingTable.ItemRule {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("cellRules", cellRules),
        )

        override fun toString(): String =
            toSimpleString()
    }

    data class ItemRuleMap(
        private val map: Map<Key, RerollingTable.ItemRule>,
    ) : RerollingTable.ItemRuleMap {
        override fun get(key: Key): RerollingTable.ItemRule? {
            return map[key]
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("map", map),
        )

        override fun toString(): String =
            toSimpleString()
    }
}