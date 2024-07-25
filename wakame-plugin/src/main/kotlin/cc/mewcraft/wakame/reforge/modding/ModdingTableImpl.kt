package cc.mewcraft.wakame.reforge.modding

import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.reforge.modding.match.CoreMatchRule
import cc.mewcraft.wakame.reforge.modding.match.CoreMatchRuleAny
import cc.mewcraft.wakame.reforge.modding.match.CurseMatchRule
import cc.mewcraft.wakame.reforge.modding.match.CurseMatchRuleAny
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
    override val globalCost: ModdingTable.GlobalCost = GlobalCostFree
    override val itemRules: ItemRuleMapAny = ItemRuleMapAny

    object GlobalCostFree : ModdingTable.GlobalCost {
        override val base: Double = 0.0
        override val perCell: Double = 0.0
        override val rarityModifiers: Map<Rarity, Double> = emptyMap()
        override val itemLevelModifier: Double = 0.0
        override val coreLevelModifier: Double = 0.0
    }

    class ItemRuleAny(
        override val target: Key,
    ) : ModdingTable.ItemRule {
        override val cellRules: ModdingTable.CellRuleMap = CellRuleMapAny
    }

    object CellRuleAny : ModdingTable.CellRule {
        override val permission: String? = null
        override val cost: Double = 0.0
        override val modLimit: Int = Int.MAX_VALUE
        override val acceptedCores: List<CoreMatchRule> = listOf(CoreMatchRuleAny)
        override val acceptedCurses: List<CurseMatchRule> = listOf(CurseMatchRuleAny)
    }

    object CellRuleMapAny : ModdingTable.CellRuleMap {
        override fun get(key: String): ModdingTable.CellRule {
            return CellRuleAny
        }
    }

    object ItemRuleMapAny : ModdingTable.ItemRuleMap {
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
    override val globalCost: ModdingTable.GlobalCost,
    override val itemRules: ModdingTable.ItemRuleMap,
) : ModdingTable {
    data class GlobalCost(
        override val base: Double,
        override val perCell: Double,
        override val rarityModifiers: Map<Rarity, Double>,
        override val itemLevelModifier: Double,
        override val coreLevelModifier: Double,
    ) : ModdingTable.GlobalCost {
        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return super.examinableProperties()
        }
    }

    data class ItemRule(
        override val target: Key,
        override val cellRules: ModdingTable.CellRuleMap,
    ) : ModdingTable.ItemRule {
        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return super.examinableProperties()
        }
    }

    data class ItemRuleMap(
        private val map: Map<Key, ModdingTable.ItemRule>,
    ) : ModdingTable.ItemRuleMap {
        override fun get(key: Key): ModdingTable.ItemRule? {
            return map[key]
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return super.examinableProperties()
        }
    }

    data class CellRule(
        override val permission: String?,
        override val cost: Double,
        override val modLimit: Int,
        override val acceptedCores: List<CoreMatchRule> = emptyList(),
        override val acceptedCurses: List<CurseMatchRule> = emptyList(),
    ) : ModdingTable.CellRule {
        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return super.examinableProperties()
        }
    }

    data class CellRuleMap(
        private val map: Map<String, ModdingTable.CellRule>,
    ) : ModdingTable.CellRuleMap {
        override fun get(key: String): ModdingTable.CellRule {
            return map[key] ?: error("No such cell rule: $key")
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return super.examinableProperties()
        }
    }
}