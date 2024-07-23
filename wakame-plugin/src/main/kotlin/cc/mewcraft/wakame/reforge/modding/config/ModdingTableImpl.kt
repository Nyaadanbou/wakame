package cc.mewcraft.wakame.reforge.modding.config

import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.reforge.modding.match.CoreMatchRule
import cc.mewcraft.wakame.reforge.modding.match.CurseMatchRule
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

internal class ModdingTableImpl : ModdingTable {
    override val enabled: Boolean
        get() = TODO("Not yet implemented")
    override val title: Component
        get() = TODO("Not yet implemented")
    override val cost: ModdingTable.GlobalCost
        get() = TODO("Not yet implemented")
    override val rules: Map<Key, ModdingTable.ItemRule>
        get() = TODO("Not yet implemented")

    class GlobalCost: ModdingTable.GlobalCost {
        override val base: Double
            get() = TODO("Not yet implemented")
        override val perCell: Double
            get() = TODO("Not yet implemented")
        override val rarityModifiers: Map<Rarity, Double>
            get() = TODO("Not yet implemented")
        override val itemLevelModifier: Double
            get() = TODO("Not yet implemented")
        override val coreLevelModifier: Double
            get() = TODO("Not yet implemented")
    }

    class ItemRule: ModdingTable.ItemRule {
        override val target: Key
            get() = TODO("Not yet implemented")
        override val cellRules: Map<String, ModdingTable.CellRule>
            get() = TODO("Not yet implemented")
    }

    class CellRule : ModdingTable.CellRule {
        override val target: String
            get() = TODO("Not yet implemented")
        override val permission: String
            get() = TODO("Not yet implemented")
        override val cost: Double
            get() = TODO("Not yet implemented")
        override val modLimit: Int
            get() = TODO("Not yet implemented")
        override val acceptedCores: List<CoreMatchRule>
            get() = TODO("Not yet implemented")
        override val acceptedCurses: List<CurseMatchRule>
            get() = TODO("Not yet implemented")
    }
}