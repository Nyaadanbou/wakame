package cc.mewcraft.wakame.reforge.modding.match

import cc.mewcraft.wakame.item.components.cells.Curse

object CurseMatchRuleAny : CurseMatchRule {
    override fun test(curse: Curse): Boolean = true
}