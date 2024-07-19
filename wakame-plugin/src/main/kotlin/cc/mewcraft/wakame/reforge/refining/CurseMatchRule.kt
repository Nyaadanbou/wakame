package cc.mewcraft.wakame.reforge.refining

import cc.mewcraft.wakame.item.components.cells.Curse

/**
 * 词条栏的诅咒的匹配规则, 用于测试一个诅咒是否符合某种规则.

 */
interface CurseMatchRule {
    fun test(curse: Curse): Boolean
}