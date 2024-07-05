package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.skill.context.SkillContext
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * 代表一个单独的技能条件.
 *
 * 技能条件即技能释放所需的条件, 如物品耐久度, 玩家魔力值等.
 *
 * ## 概念
 *
 * 我们把每次执行条件判断看成是一次`会话`([SkillConditionSession]).
 * 整个流程始于调用 [newSession] 创建一个新的 [SkillConditionSession].
 *
 * ## [会话][SkillConditionSession]
 * 一个 [SkillConditionSession] 有如下功能:
 * 1. 用 [SkillConditionSession.isSuccess] 检查条件是否`满足`
 * 2. 用 [SkillConditionSession.onSuccess] 执行条件`满足`时发生的逻辑
 * 3. 用 [SkillConditionSession.onFailure] 执行条件`不满足`时发生的逻辑
 *
 * ## 实现
 *
 * 不要实现该接口, 直接继承 [SkillConditionBase].
 */
interface SkillCondition {
    /**
     * 条件的类型.
     */
    val type: String

    /**
     * 条件的优先级. 数值越高, 优先级越高.
     *
     * 主要用于 [SkillConditionGroup] 的实现.
     *
     * 多个条件存在时, 优先级高的条件将被优先处理. 条件是否满足的逻辑采取的是
     * "fast-fail" - 如果第一个条件不满足, 那么后面的条件将被跳过不会执行.
     */
    val priority: Int

    /**
     * 用于创建技能的文字描述信息.
     */
    val resolver: TagResolver // TODO 单独弄个接口?

    /**
     * 创建一个新的条件判断的会话.
     */
    fun newSession(context: SkillContext): SkillConditionSession
}

/**
 * 定义了如何从配置文件 ([ConfigProvider]) 创建一个 [SkillCondition] 的实例.
 *
 * ## 实现方式
 *
 * 在 [SkillCondition] 的实现里创建一个 `companion object`, 让其实现 [SkillConditionFactory].
 */
interface SkillConditionFactory<C : SkillCondition> {
    fun create(config: ConfigProvider): C
}
