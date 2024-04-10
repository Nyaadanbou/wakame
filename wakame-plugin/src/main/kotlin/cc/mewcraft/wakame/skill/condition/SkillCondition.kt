package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.condition.Condition

/**
 * 技能释放所需的条件，如物品耐久度、玩家魔力值等。
 */
interface SkillCondition<in T : SkillContext> : Condition<T> {
//    var isPass: Boolean
    override fun test(context: T): Boolean
    fun cost(context: T)
}

/**
 * 技能释放所需的上下文，如玩家、物品等。
 */
interface SkillContext