package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainer
import cc.mewcraft.wakame.reforge.common.RarityNumberMapping
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.bindInstance
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
    override val id: String = "wtf"

    override val primaryMenuSettings: BasicMenuSettings = BasicMenuSettings(
        title = Component.text("Modding Table (Cheat Mode)"),
        structure = arrayOf(
            ". . . x x x . . .",
            ". . . x x x . . .",
            ". i . x x x . o .",
            ". . . x x x . . .",
            ". . . x x x . . .",
            ". . . < . > . . .",
        ),
        icons = hashMapOf(
            "background" to Key.key("internal:menu/common/default/background"),
            "prev_page" to Key.key("internal:menu/common/default/prev_page"),
            "next_page" to Key.key("internal:menu/common/default/next_page"),
            "input_ok" to Key.key("internal:menu/modding_table/default/input_ok"),
            "input_empty" to Key.key("internal:menu/modding_table/default/input_empty"),
            "output_ok_confirmed" to Key.key("internal:menu/modding_table/default/output_ok_confirmed"),
            "output_ok_unconfirmed" to Key.key("internal:menu/modding_table/default/output_ok_unconfirmed"),
            "output_empty" to Key.key("internal:menu/modding_table/default/output_empty"),
            "output_failure" to Key.key("internal:menu/modding_table/default/output_failure"),
            "output_insufficient_resource" to Key.key("internal:menu/modding_table/default/output_insufficient_resource"),
        )
    )
    override val replaceMenuSettings: BasicMenuSettings = BasicMenuSettings(
        title = Component.text("DO_NOT_USE"),
        structure = arrayOf("a * b"),
        icons = hashMapOf(
            "compatibility_view" to Key.key("internal:menu/modding_table/default/compatibility_view"),
            "core_view" to Key.key("internal:menu/modding_table/default/core_view"),
            "core_unusable" to Key.key("internal:menu/modding_table/default/core_unusable"),
            "core_usable" to Key.key("internal:menu/modding_table/default/core_usable"),
        )
    )

    override val reforgeCountAddMethod: ModdingTable.ReforgeCountAddMethod = ModdingTable.ReforgeCountAddMethod.PLUS_ONE

    override val rarityNumberMapping: RarityNumberMapping = RarityNumberMapping.constant(1.0)

    override val currencyCost: ModdingTable.CurrencyCost<ModdingTable.TableTotalFunction> = ZeroTableCurrencyCost

    override val itemRuleMap: ModdingTable.ItemRuleMap = AnyItemRuleMap

    override fun toString(): String = toSimpleString()

    private val ZERO_MOCHA_FUNCTION = MochaFunction { .0 }

    private class AnyItemRule(
        override val itemId: Key,
    ) : ModdingTable.ItemRule {
        override val modLimit: Int = Int.MAX_VALUE
        override val coreRuleMap: ModdingTable.CoreRuleMap = AnyCoreRuleMap
    }

    private object AnyCellRule : ModdingTable.CellRule {
        override val requireElementMatch: Boolean = false
        override val currencyCost: ModdingTable.CurrencyCost<ModdingTable.CellTotalFunction> = ZeroCellCurrencyCost
        override val permission: String? = null
        override val acceptableCores: CoreMatchRuleContainer = CoreMatchRuleContainer.any()
    }

    private object AnyCoreRuleMap : ModdingTable.CoreRuleMap {
        override val comparator: Comparator<String?> = nullsLast(naturalOrder())
        override fun get(key: String): ModdingTable.CellRule = AnyCellRule
    }

    private object AnyItemRuleMap : ModdingTable.ItemRuleMap {
        override fun get(key: Key): ModdingTable.ItemRule = AnyItemRule(key)
        override fun contains(key: Key): Boolean = true
    }

    private object ZeroTableCurrencyCost : ModdingTable.CurrencyCost<ModdingTable.TableTotalFunction> {
        override val total = ModdingTable.TableTotalFunction { ZERO_MOCHA_FUNCTION }
    }

    private object ZeroCellCurrencyCost : ModdingTable.CurrencyCost<ModdingTable.CellTotalFunction> {
        override val total = ModdingTable.CellTotalFunction { _, _ -> ZERO_MOCHA_FUNCTION }
    }
}

/**
 * 一个标准的定制台, 需要从配置文件中构建.
 */
internal class SimpleModdingTable(
    override val id: String,
    override val primaryMenuSettings: BasicMenuSettings,
    override val replaceMenuSettings: BasicMenuSettings,
    override val reforgeCountAddMethod: ModdingTable.ReforgeCountAddMethod,
    override val rarityNumberMapping: RarityNumberMapping,
    override val currencyCost: ModdingTable.CurrencyCost<ModdingTable.TableTotalFunction>,
    override val itemRuleMap: ModdingTable.ItemRuleMap,
) : ModdingTable {

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id),
        ExaminableProperty.of("primaryMenuSettings", primaryMenuSettings),
        ExaminableProperty.of("replaceMenuSettings", replaceMenuSettings),
        ExaminableProperty.of("rarityNumberMapping", rarityNumberMapping),
        ExaminableProperty.of("currencyCost", currencyCost),
        ExaminableProperty.of("itemRules", itemRuleMap),
    )

    override fun toString(): String =
        toSimpleString()

    class ItemRule(
        override val itemId: Key,
        override val modLimit: Int,
        override val coreRuleMap: ModdingTable.CoreRuleMap,
    ) : ModdingTable.ItemRule {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("itemId", itemId),
            ExaminableProperty.of("modLimit", modLimit),
            ExaminableProperty.of("coreRuleMap", coreRuleMap),
        )

        override fun toString(): String = toSimpleString()
    }

    class ItemRuleMap(
        private val data: Map<Key, ModdingTable.ItemRule>,
    ) : ModdingTable.ItemRuleMap {
        override fun get(key: Key): ModdingTable.ItemRule? {
            return data[key]
        }

        override fun contains(key: Key): Boolean {
            return data.containsKey(key)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("map", data),
        )

        override fun toString(): String = toSimpleString()
    }

    class CellRule(
        override val requireElementMatch: Boolean,
        override val currencyCost: ModdingTable.CurrencyCost<ModdingTable.CellTotalFunction>,
        override val permission: String?,
        override val acceptableCores: CoreMatchRuleContainer,
    ) : ModdingTable.CellRule {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("requireElementMatch", requireElementMatch),
            ExaminableProperty.of("currencyCost", currencyCost),
            ExaminableProperty.of("permission", permission),
            ExaminableProperty.of("acceptedCores", acceptableCores),
        )

        override fun toString(): String = toSimpleString()
    }

    class CoreRuleMap(
        private val data: LinkedHashMap<String, ModdingTable.CellRule>,
    ) : ModdingTable.CoreRuleMap {

        // 让 string 的顺序采用 key 在 data 里的顺序
        private val keyOrder: Map<String, Int> = data.keys.withIndex().associate { it.value to it.index }
        override val comparator: Comparator<String?> = nullsLast(compareBy(keyOrder::get))

        override fun get(key: String): ModdingTable.CellRule? {
            return data[key]
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("data", data)
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
            return mocha.prepareEval(code)
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
            return mocha.prepareEval(code)
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
    fun getSourceItemRarity(): Double {
        return session.getSourceItemRarityNumber()
    }

    @Binding("source_item_level")
    fun getSourceItemLevel(): Int {
        return session.getSourceItemLevel()
    }

    @Binding("source_item_total_cell_count")
    fun getSourceItemTotalCellCount(): Int {
        return session.getSourceItemTotalCellCount()
    }

    @Binding("source_item_changeable_cell_count")
    fun getSourceItemChangeableCellCount(): Int {
        return session.getSourceItemChangeableCellCount()
    }

    @Binding("source_item_changed_cell_count")
    fun getSourceItemChangedCellCount(): Int {
        return session.getSourceItemChangedCellCount()
    }

    @Binding("source_item_changed_cell_cost")
    fun getSourceItemChangedCellCost(): Double {
        return session.getSourceItemChangedCellCost()
    }
}

@Binding("query")
internal class CellTotalBinding(
    val session: ModdingSession,
    val replace: ModdingSession.Replace,
) {
    @Binding("source_item_level")
    fun getSourceItemLevel(): Int {
        return session.getSourceItemLevel()
    }

    @Binding("source_item_mod_count")
    fun getSourceItemModCount(): Int {
        return session.usableInput?.getData(ItemDataTypes.REFORGE_HISTORY)?.modCount ?: 0
    }

    @Binding("joined_item_level")
    fun getJoinedItemLevel(): Int {
        return replace.getIngredientLevel()
    }

    @Binding("joined_item_rarity")
    fun getJoinedItemRarity(): Double {
        return replace.getIngredientRarityNumber()
    }
}