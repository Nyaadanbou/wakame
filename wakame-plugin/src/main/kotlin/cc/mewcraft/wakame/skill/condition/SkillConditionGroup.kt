package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.skill.context.SkillContext
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * 代表一个技能条件组, 本质上是多个技能条件的集合.
 *
 * 外部可以将其当成一个整体, 看成是单个技能条件.
 *
 * 技能条件组的判断逻辑:
 * - 判断为 `满足`: 条件组内的每个技能条件都满足.
 * - 判断为 `不满足`: 条件组内存在一个技能条件不满足.
 *
 * 优先级高的条件会被优先执行和判断. 一旦有一个条件不满足,
 * 那么在那之后, 优先级低的技能条件将被直接跳过而不会执行.
 */
interface SkillConditionGroup {
    /**
     * 技能条件组里所有的 [SkillCondition.resolver] 之和.
     */
    fun getResolver(time: ConditionTime = ConditionTime.CAST_POINT): TagResolver

    /**
     * 创建一个新的条件判断的会话.
     */
    fun newSession(time: ConditionTime, context: SkillContext): SkillConditionSession

    companion object {
        /**
         * 返回一个空的技能条件组.
         *
         * 空的技能条件组不包含任何条件, 因此逻辑上技能永远可以释放.
         */
        fun empty(): SkillConditionGroup = EmptySkillConditionGroup
    }
}

//
// Internals
//

private data object EmptySkillConditionGroup : SkillConditionGroup {
    override fun getResolver(time: ConditionTime): TagResolver = TagResolver.empty()
    override fun newSession(time: ConditionTime, context: SkillContext): SkillConditionSession {
        return SkillConditionSession.alwaysSuccess()
    }
}
