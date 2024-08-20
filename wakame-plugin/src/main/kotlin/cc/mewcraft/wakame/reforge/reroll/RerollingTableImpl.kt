package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.reforge.common.RarityNumberMapping
import cc.mewcraft.wakame.util.bindInstance
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.ExaminableProperty
import team.unnamed.mocha.MochaEngine
import team.unnamed.mocha.runtime.MochaFunction
import team.unnamed.mocha.runtime.binding.Binding
import java.util.stream.Stream

/**
 * 一个无限制的 [RerollingTable] 实现.
 */
internal object WtfRerollingTable : RerollingTable {
    override val identifier: String = "wtf"

    override val enabled: Boolean = true

    override val title: Component = Component.text("Rerolling Table (Cheat ON)")

    override val rarityNumberMapping: RarityNumberMapping = RarityNumberMapping.constant(1.0)

    private val ZERO_MOCHA_FUNCTION = MochaFunction { .0 }

    override val currencyCost: RerollingTable.TableCurrencyCost = RerollingTable.TableCurrencyCost { ZERO_MOCHA_FUNCTION }

    override val itemRuleMap: RerollingTable.ItemRuleMap = object : RerollingTable.ItemRuleMap {
        override fun get(key: Key): RerollingTable.ItemRule = AnyItemRule
        override fun contains(key: Key): Boolean = true
    }

    override fun toString(): String = toSimpleString() // 最简输出

    private data object AnyCellRule : RerollingTable.CellRule {
        override val currencyCost: RerollingTable.CellCurrencyCost = RerollingTable.CellCurrencyCost { _, _ -> ZERO_MOCHA_FUNCTION }
        override val maxReroll: Int = Int.MAX_VALUE
    }

    private data object AnyCellRuleMap : RerollingTable.CellRuleMap {
        override fun get(key: String): RerollingTable.CellRule = AnyCellRule
        override fun contains(key: String): Boolean = true
    }

    private data object AnyItemRule : RerollingTable.ItemRule {
        override val cellRuleMap: RerollingTable.CellRuleMap = AnyCellRuleMap
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
    override val currencyCost: RerollingTable.TableCurrencyCost,
    override val itemRuleMap: RerollingTable.ItemRuleMap,
) : RerollingTable {

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("identifier", identifier),
        ExaminableProperty.of("enabled", enabled),
        ExaminableProperty.of("title", title),
        ExaminableProperty.of("rarityNumberMapping", rarityNumberMapping),
        ExaminableProperty.of("currencyCost", this@SimpleRerollingTable.currencyCost),
        ExaminableProperty.of("itemRuleMap", itemRuleMap),
    )

    override fun toString(): String = toSimpleString()

    data class CellRule(
        override val maxReroll: Int,
        override val currencyCost: RerollingTable.CellCurrencyCost,
    ) : RerollingTable.CellRule {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("currencyCost", currencyCost),
            ExaminableProperty.of("maxReroll", maxReroll),
        )

        override fun toString(): String = toSimpleString()
    }

    data class CellRuleMap(
        private val map: Map<String, RerollingTable.CellRule>,
    ) : RerollingTable.CellRuleMap {
        override fun get(key: String): RerollingTable.CellRule? {
            return map[key]
        }

        override fun contains(key: String): Boolean {
            return map.containsKey(key)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("map", map),
        )

        override fun toString(): String = toSimpleString()
    }

    data class ItemRule(
        override val cellRuleMap: RerollingTable.CellRuleMap,
    ) : RerollingTable.ItemRule {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("cellRuleMap", cellRuleMap),
        )

        override fun toString(): String = toSimpleString()
    }

    data class ItemRuleMap(
        private val map: Map<Key, RerollingTable.ItemRule>,
    ) : RerollingTable.ItemRuleMap {
        override fun get(key: Key): RerollingTable.ItemRule? {
            return map[key]
        }

        override fun contains(key: Key): Boolean {
            return map.containsKey(key)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("map", map),
        )

        override fun toString(): String = toSimpleString()
    }

    data class TableCurrencyCost(
        val code: String
    ) : RerollingTable.TableCurrencyCost {
        override fun compile(session: RerollingSession): MochaFunction {
            val mocha = MochaEngine.createStandard()
            val binding = TableCostBinding(session)
            mocha.bindInstance(binding, "query")
            return mocha.prepareEval(code)
        }
    }

    data class CellCurrencyCost(
        val code: String,
    ) : RerollingTable.CellCurrencyCost {
        override fun compile(session: RerollingSession, selection: RerollingSession.Selection): MochaFunction {
            val mocha = MochaEngine.createStandard()
            val binding = CellCostBinding(session, selection)
            mocha.bindInstance(binding, "query")
            return mocha.prepareEval(code)
        }
    }
}

@Binding("query")
internal class TableCostBinding(
    val session: RerollingSession,
) {
    @Binding("source_rarity")
    fun sourceRarity(): Double {
        val mapping = session.table.rarityNumberMapping
        val rarity = session.sourceItem?.components?.get(ItemComponentTypes.RARITY)?.rarity?.key ?: return .0
        return mapping.get(rarity)
    }

    @Binding("source_level")
    fun sourceLevel(): Int {
        return session.sourceItem?.components?.get(ItemComponentTypes.LEVEL)?.level?.toInt() ?: 0
    }

    @Binding("cell_count")
    fun countCell(type: String): Int {
        val selectionMap = session.selectionMap
        return when (type) {
            "all" -> selectionMap.size
            "selected" -> selectionMap.values.count { it.selected }
            "unselected" -> selectionMap.values.count { !it.selected }
            else -> throw IllegalArgumentException("Unknown type: '$type'")
        }
    }

    @Binding("sum_of_cost")
    fun sumOfCost(type: String): Double {
        val selections = session.selectionMap
        return when (type) {
            "all" -> selections.values.sumOf { it.total.evaluate() }
            "selected" -> selections.values.sumOf { if (it.selected) it.total.evaluate() else .0 }
            "unselected" -> selections.values.sumOf { if (!it.selected) it.total.evaluate() else .0 }
            else -> throw IllegalArgumentException("Unknown type: '$type'")
        }
    }
}

@Binding("query")
internal class CellCostBinding(
    val session: RerollingSession,
    val selection: RerollingSession.Selection,
) {
    @Binding("max_reroll")
    fun maxReroll(): Int {
        return selection.rule.maxReroll
    }

    @Binding("reroll_count")
    fun rerollCount(): Int {
        val sourceItem = session.sourceItem
        val sourceCells = sourceItem?.components?.get(ItemComponentTypes.CELLS) ?: return 0
        val rerollCount = sourceCells.get(selection.id)?.getReforgeHistory()?.rerollCount ?: 0
        return rerollCount
    }
}