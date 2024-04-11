package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.skill.SkillContext

/**
 * 技能释放所需的条件，如物品耐久度、玩家魔力值等。
 */
interface SkillCondition<in T : SkillContext> : Condition<T> {
    override fun test(context: T): Boolean
    fun cost(context: T)
}

interface SkillConditionFactory<T : SkillContext, C : SkillCondition<T>> {
    fun provide(config: ConfigProvider): C
}