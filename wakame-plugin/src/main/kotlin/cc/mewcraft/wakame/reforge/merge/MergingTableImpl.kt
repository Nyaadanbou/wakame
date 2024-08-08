package cc.mewcraft.wakame.reforge.merge

/**
 * 一个没有任何限制的合并台.
 */
internal object WtfMergingTable : MergingTable {
    override val enabled: Boolean = true
    override val title: String = "Merging Table (Cheat ON)"
    override val cost: MergingTable.Cost
        get() = TODO("Not yet implemented")
    override val rules: MergingTable.ItemRuleMap
        get() = TODO("Not yet implemented")
}