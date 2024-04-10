package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.skill.Caster

/**
 * 技能释放所需的条件，如物品耐久度、玩家魔力值等。
 */
interface Condition {
//    var isPass: Boolean
    fun test(): Boolean
    fun cost()
}