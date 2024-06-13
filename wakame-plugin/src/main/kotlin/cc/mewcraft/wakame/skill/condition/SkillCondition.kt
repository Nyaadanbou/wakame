package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.skill.context.SkillCastContext
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * 代表一个单独的技能条件。
 *
 * 技能条件即技能释放所需的条件，如物品耐久度、玩家魔力值等。
 *
 * 在配置文件里代表列表中的一个条件。
 */
interface SkillCondition : Condition<SkillCastContext>, Comparable<SkillCondition> {
    val id: String
    val priority: Priority

    val tagResolver: TagResolver

    override fun test(context: SkillCastContext): Boolean

    fun cost(context: SkillCastContext)

    fun notifyFailure(context: SkillCastContext)

    enum class Priority {
        LOWEST,
        LOW,
        NORMAL,
        HIGH,
        HIGHEST;
    }

    override fun compareTo(other: SkillCondition): Int {
        if (priority == other.priority) {
            return id.compareTo(other.id)
        }
        return priority.compareTo(other.priority)
    }
}

/**
 * 代表一个无消耗的技能条件。(也就是只有判断功能)
 */
interface NoCostSkillCondition : SkillCondition {
    override fun cost(context: SkillCastContext) = Unit
}

interface SkillConditionFactory<C : SkillCondition> {
    fun provide(config: ConfigProvider): C
}