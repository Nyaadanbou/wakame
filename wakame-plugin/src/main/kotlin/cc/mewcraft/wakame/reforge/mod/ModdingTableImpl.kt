package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainer
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
 * 一个没有任何限制的定制台.
 */
internal object WtfModdingTable : ModdingTable {
    override val identifier: String = "wtf"

    override val enabled: Boolean = true

    override val title: Component = Component.text("Modding Table (Cheat ON)")

    override val rarityNumberMapping: RarityNumberMapping = RarityNumberMapping.constant(1.0)

    override val currencyCost: ModdingTable.CurrencyCost<ModdingTable.TableTotalFunction> = ZeroTableCurrencyCost

    override val itemRules: ModdingTable.ItemRuleMap = AnyItemRuleMap

    override fun toString(): String = toSimpleString()

    private class AnyItemRule(
        override val target: Key,
    ) : ModdingTable.ItemRule {
        override val cellRules: ModdingTable.CellRuleMap = AnyCellRuleMap
    }

    private object AnyCellRule : ModdingTable.CellRule {
        override val modLimit: Int = Int.MAX_VALUE
        override val requireElementMatch: Boolean = false
        override val currencyCost: ModdingTable.CurrencyCost<ModdingTable.CellTotalFunction> = ZeroCellCurrencyCost
        override val permission: String? = null
        override val acceptedCores: CoreMatchRuleContainer = CoreMatchRuleContainer.any()
    }

    private object AnyCellRuleMap : ModdingTable.CellRuleMap {
        override fun get(key: String): ModdingTable.CellRule = AnyCellRule
    }

    private object AnyItemRuleMap : ModdingTable.ItemRuleMap {
        override fun get(key: Key): ModdingTable.ItemRule = AnyItemRule(key)
        override fun contains(key: Key): Boolean = true
    }

    private val ZERO_MOCHA_FUNCTION = MochaFunction { .0 }

    private object ZeroTableCurrencyCost : ModdingTable.CurrencyCost<ModdingTable.TableTotalFunction> {
        override val total = ModdingTable.TableTotalFunction { ZERO_MOCHA_FUNCTION }
    }

    private object ZeroCellCurrencyCost : ModdingTable.CurrencyCost<ModdingTable.CellTotalFunction> {
        override val total = ModdingTable.CellTotalFunction { session, replace -> ZERO_MOCHA_FUNCTION }
    }
}

/**
 * 一个标准的定制台, 需要从配置文件中构建.
 */
internal class SimpleModdingTable(
    override val identifier: String,
    override val enabled: Boolean,
    override val title: Component,
    override val rarityNumberMapping: RarityNumberMapping,
    override val currencyCost: ModdingTable.CurrencyCost<ModdingTable.TableTotalFunction>,
    override val itemRules: ModdingTable.ItemRuleMap,
) : ModdingTable {

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

    class ItemRule(
        override val target: Key,
        override val cellRules: ModdingTable.CellRuleMap,
    ) : ModdingTable.ItemRule {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("target", target),
            ExaminableProperty.of("cellRules", cellRules),
        )

        override fun toString(): String = toSimpleString()
    }

    class ItemRuleMap(
        private val map: Map<Key, ModdingTable.ItemRule>,
    ) : ModdingTable.ItemRuleMap {
        override fun get(key: Key): ModdingTable.ItemRule? {
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

    class CellRule(
        override val modLimit: Int,
        override val requireElementMatch: Boolean,
        override val currencyCost: ModdingTable.CurrencyCost<ModdingTable.CellTotalFunction>,
        override val permission: String?,
        override val acceptedCores: CoreMatchRuleContainer
    ) : ModdingTable.CellRule {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("modLimit", modLimit),
            ExaminableProperty.of("requireElementMatch", requireElementMatch),
            ExaminableProperty.of("currencyCost", currencyCost),
            ExaminableProperty.of("permission", permission),
            ExaminableProperty.of("acceptedCores", acceptedCores),
        )

        override fun toString(): String = toSimpleString()
    }

    class CellRuleMap(
        private val map: Map<String, ModdingTable.CellRule>,
    ) : ModdingTable.CellRuleMap {
        override fun get(key: String): ModdingTable.CellRule? {
            return map[key]
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("map", map)
        )

        override fun toString(): String = toSimpleString()
    }

    class TableCurrencyCost(
        override val total: ModdingTable.TableTotalFunction,
    ) : ModdingTable.CurrencyCost<ModdingTable.TableTotalFunction> {
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("total", total)
        )
        override fun toString(): String = toSimpleString()
    }

    class CellCurrencyCost(
        override val total: ModdingTable.CellTotalFunction,
    ) : ModdingTable.CurrencyCost<ModdingTable.CellTotalFunction> {
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("total", total)
        )
        override fun toString(): String = toSimpleString()
    }

    class TableTotalFunction(
        val code: String,
    ) : ModdingTable.TableTotalFunction {
        override fun compile(session: ModdingSession): MochaFunction {
            val mocha = MochaEngine.createStandard()
            val binding = TableTotalBinding(session)
            mocha.bindInstance(binding, "query")
            return mocha.compile(code)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("code", code)
        )

        override fun toString(): String {
            return toSimpleString()
        }
    }

    class CellTotalFunction(
        val code: String,
    ) : ModdingTable.CellTotalFunction {
        override fun compile(session: ModdingSession, replace: ModdingSession.Replace): MochaFunction {
            val mocha = MochaEngine.createStandard()
            val binding = CellTotalBinding(session, replace)
            mocha.bindInstance(binding, "query")
            return mocha.compile(code)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("code", code)
        )

        override fun toString(): String {
            return toSimpleString()
        }
    }
}

@Binding("query")
internal class TableTotalBinding(
    val session: ModdingSession,
) {
    @Binding("source_item_rarity")
    fun sourceItemRarity(): Double {
        return session.getSourceItemRarityNumber()
    }

    @Binding("source_item_level")
    fun sourceItemLevel(): Int {
        return session.getSourceItemLevel()
    }

    @Binding("source_item_total_cell_count")
    fun sourceItemTotalCellCount(): Int {
        return session.getSourceItemTotalCellCount()
    }

    @Binding("source_item_changeable_cell_count")
    fun sourceItemChangeableCellCount(): Int {
        return session.getSourceItemChangeableCellCount()
    }

    @Binding("source_item_changed_cell_count")
    fun sourceItemChangedCellCount(): Int {
        return session.getSourceItemChangedCellCount()
    }

    @Binding("source_item_changed_cell_cost")
    fun sourceItemChangedCellCost(): Double {
        return session.getSourceItemChangedCellCost()
    }
}

@Binding("query")
internal class CellTotalBinding(
    val session: ModdingSession,
    val replace: ModdingSession.Replace,
) {
    @Binding("source_item_level")
    fun sourceItemLevel(): Int {
        return session.getSourceItemLevel()
    }

    @Binding("joined_item_level")
    fun joinedItemLevel(): Int {
        return replace.getIngredientLevel()
    }

    @Binding("joined_item_rarity")
    fun joinedItemRarity(): Double {
        return replace.getIngredientRarityNumber()
    }
}