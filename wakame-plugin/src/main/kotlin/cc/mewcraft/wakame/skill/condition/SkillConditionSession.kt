package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.skill.context.SkillContext

/**
 * 代表执行一次条件判断的会话.
 *
 * ## 用法
 * 首先使用 [isSuccess] 检查条件是否 `满足`. 然后根据检查的结果, 调用 [onSuccess]
 * 来执行当条件 `满足` 时所触发的逻辑, 或者 [onFailure] 来执行当条件 `不满足`
 * 时所触发的逻辑.
 */
interface SkillConditionSession {
    /**
     * 检查条件是否满足.
     */
    val isSuccess: Boolean

    /**
     * 当条件**满足时**执行的逻辑. 可能会修改 [context] 的状态.
     *
     * 这包括消耗相应的资源, 发送消息提示等等.
     */
    fun onSuccess(context: SkillContext)

    /**
     * 当条件**不满足时**执行的逻辑. 可能会修改 [context] 的状态.
     */
    fun onFailure(context: SkillContext)

    companion object {
        /**
         * 返回一个永远 `满足` 的 [SkillConditionSession].
         */
        fun alwaysSuccess(): SkillConditionSession = AlwaysSuccessSession

        /**
         * 返回一个永远 `不满足` 的 [SkillConditionSession].
         */
        fun alwaysFailure(): SkillConditionSession = AlwaysFailureSession
    }
}

//
// Internals
//

private data object AlwaysSuccessSession : SkillConditionSession {
    override val isSuccess: Boolean = true
    override fun onSuccess(context: SkillContext) {}
    override fun onFailure(context: SkillContext) {}
}

private data object AlwaysFailureSession : SkillConditionSession {
    override val isSuccess: Boolean = false
    override fun onSuccess(context: SkillContext) {}
    override fun onFailure(context: SkillContext) {}
}
