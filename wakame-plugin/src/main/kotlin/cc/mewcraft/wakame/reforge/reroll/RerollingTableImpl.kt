package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.reforge.common.RarityNumberMapping
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 一个无限制的 [RerollingTable] 实现.
 */
internal object WtfRerollingTable : RerollingTable {
    override val identifier: String = "wtf"

    override val enabled: Boolean = true

    override val title: Component = Component.text("Rerolling Table (Cheat ON)")

    override val rarityNumberMapping: RarityNumberMapping = object : RarityNumberMapping {
        override fun get(key: Key): Double = 1.0
    }

    override val currencyCost: RerollingTable.CurrencyCost = object : RerollingTable.CurrencyCost {
        override val base: Double = 0.0
        override val eachFunction: RerollingTable.CurrencyCost.EachFunction = object : RerollingTable.CurrencyCost.EachFunction {
            override fun compute(
                cost: Double, maxReroll: Int, rerollCount: Int
            ): Double = .0
        }
        override val totalFunction: RerollingTable.CurrencyCost.TotalFunction = object : RerollingTable.CurrencyCost.TotalFunction {
            override fun compute(
                base: Double, rarity: Double, itemLevel: Int, allCount: Int, selectedCount: Int, selectedCostSum: Double, unselectedCostSum: Double
            ): Double = .0
        }

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("base", base),
        )

        override fun toString(): String =
            toSimpleString()
    }

    override val itemRules: RerollingTable.ItemRuleMap = object :RerollingTable.ItemRuleMap {
        override fun get(key: Key): RerollingTable.ItemRule {
            return AnyItemRule
        }
    }

    override fun toString(): String {
        return toSimpleString() // 最简输出
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
}

/**
 * 一个标准的 [RerollingTable] 实现, 设计上需要从配置文件构建.
 */
internal class SimpleRerollingTable(
    override val identifier: String,
    override val enabled: Boolean,
    override val title: Component,
    override val rarityNumberMapping: RarityNumberMapping,
    override val currencyCost: RerollingTable.CurrencyCost,
    override val itemRules: RerollingTable.ItemRuleMap,
) : RerollingTable {

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("identifier", identifier),
        ExaminableProperty.of("enabled", enabled),
        ExaminableProperty.of("title", title),
        ExaminableProperty.of("rarityNumberMapping", rarityNumberMapping),
        ExaminableProperty.of("currencyCost", currencyCost),
        ExaminableProperty.of("itemRules", itemRules),
    )

    override fun toString(): String =
        toSimpleString()

    data class CurrencyCost(
        override val base: Double,
        override val eachFunction: RerollingTable.CurrencyCost.EachFunction,
        override val totalFunction: RerollingTable.CurrencyCost.TotalFunction,
    ) : RerollingTable.CurrencyCost {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("base", base),
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