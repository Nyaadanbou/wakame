package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.config.ConfigProvider

/**
 * 代表一个单独的技能条件。
 *
 * 技能条件即技能释放所需的条件，如物品耐久度、玩家魔力值等。
 *
 * 在配置文件里代表列表中的一个条件。
 */
interface SkillCondition : Condition<SkillCastContext> {
    val priority: Priority

    override fun test(context: SkillCastContext): Boolean

    fun cost(context: SkillCastContext)

    fun notifyFailure(context: SkillCastContext)

    enum class Priority {
        HIGHEST,
        HIGH,
        NORMAL,
        LOW,
        LOWEST
    }
}

interface SkillConditionFactory<C : SkillCondition> {
    fun provide(config: ConfigProvider): C
}