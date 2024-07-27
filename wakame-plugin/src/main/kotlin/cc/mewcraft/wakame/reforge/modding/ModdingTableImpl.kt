package cc.mewcraft.wakame.reforge.modding

import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 一个没有任何限制的定制台.
 */
object ModdingTableWtf : ModdingTable {
    override val enabled: Boolean = true
    override val title: Component = Component.text("Wtf Modding Table")
    override val cost: ModdingTable.Cost = CostFree
    override val itemRules: ModdingTable.ItemRuleMap = ItemRuleMapAny

    data object CostFree : ModdingTable.Cost {
        override val base: Double = 0.0
        override val perCore: Double = 0.0
        override val perCurse: Double = 0.0
        override val rarityModifiers: Map<Key, Double> = emptyMap()
        override val itemLevelModifier: Double = 0.0
        override val coreLevelModifier: Double = 0.0
    }

    data class ItemRuleAny(
        override val target: Key,
    ) : ModdingTable.ItemRule {
        override val cellRules: ModdingTable.CellRuleMap = CellRuleMapAny
    }

    data object CellRuleAny : ModdingTable.CellRule {
        override val permission: String? = null
        override val cost: Double = 0.0
        override val modLimit: Int = Int.MAX_VALUE
        override val acceptedCores: List<CoreMatchRule> = listOf(CoreMatchRuleAny)
        override val acceptedCurses: List<CurseMatchRule> = listOf(CurseMatchRuleAny)
    }

    data object CellRuleMapAny : ModdingTable.CellRuleMap {
        override fun get(key: String): ModdingTable.CellRule {
            return CellRuleAny
        }
    }

    data object ItemRuleMapAny : ModdingTable.ItemRuleMap {
        override fun get(key: Key): ModdingTable.ItemRule {
            return ItemRuleAny(key)
        }
    }
}

/**
 * 一个标准的定制台, 需要从配置文件中构建.
 */
internal class ModdingTableImpl(
    override val enabled: Boolean,
    override val title: Component,
    override val cost: ModdingTable.Cost,
    override val itemRules: ModdingTable.ItemRuleMap,
) : ModdingTable {
    data class Cost(
        override val base: Double,
        override val perCore: Double,
        override val perCurse: Double,
        override val rarityModifiers: Map<Key, Double>,
        override val itemLevelModifier: Double,
        override val coreLevelModifier: Double,
    ) : ModdingTable.Cost {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("base", base),
            ExaminableProperty.of("perCell", perCore),
            ExaminableProperty.of("rarityModifiers", rarityModifiers),
            ExaminableProperty.of("itemLevelModifier", itemLevelModifier),
            ExaminableProperty.of("coreLevelModifier", coreLevelModifier),
        )

        override fun toString(): String = toSimpleString()
    }

    data class ItemRule(
        override val target: Key,
        override val cellRules: ModdingTable.CellRuleMap,
    ) : ModdingTable.ItemRule {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("target", target),
            ExaminableProperty.of("cellRules", cellRules),
        )

        override fun toString(): String = toSimpleString()
    }

    data class ItemRuleMap(
        val map: Map<Key, ModdingTable.ItemRule>,
    ) : ModdingTable.ItemRuleMap {
        override fun get(key: Key): ModdingTable.ItemRule? {
            return map[key]
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("map", map),
        )

        override fun toString(): String = toSimpleString()
    }

    data class CellRule(
        override val permission: String?,
        override val cost: Double,
        override val modLimit: Int,
        override val acceptedCores: List<CoreMatchRule> = emptyList(),
        override val acceptedCurses: List<CurseMatchRule> = emptyList(),
    ) : ModdingTable.CellRule {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("permission", permission),
            ExaminableProperty.of("cost", cost),
            ExaminableProperty.of("modLimit", modLimit),
            ExaminableProperty.of("acceptedCores", acceptedCores),
            ExaminableProperty.of("acceptedCurses", acceptedCurses),
        )

        override fun toString(): String = toSimpleString()
    }

    data class CellRuleMap(
        val map: Map<String, ModdingTable.CellRule>,
    ) : ModdingTable.CellRuleMap {
        override fun get(key: String): ModdingTable.CellRule {
            return map[key] ?: error("No such cell rule: $key")
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("map", map)
        )

        override fun toString(): String = toSimpleString()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("enabled", enabled),
        ExaminableProperty.of("title", title),
        ExaminableProperty.of("cost", cost),
        ExaminableProperty.of("itemRules", itemRules),
    )

    override fun toString(): String = toSimpleString()
}