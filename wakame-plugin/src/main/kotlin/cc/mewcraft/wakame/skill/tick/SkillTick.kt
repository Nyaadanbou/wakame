package cc.mewcraft.wakame.skill.tick

import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.TriggerConditions
import cc.mewcraft.wakame.skill.condition.ConditionTime
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.state.SkillStateInfo
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.tick.Tickable
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 表示技能触发的效果.
 */
sealed interface SkillTick<S : Skill> : Tickable, Examinable {

    companion object {
        /**
         * 一个空的技能触发效果.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : SkillTick<*>> empty(): T = EmptyPlayerSkillTick as T
    }

    /**
     * 此次 Tick 的技能
     */
    val skill: S

    /**
     * 此次触发效果的上下文.
     */
    val context: SkillContext

    override fun tick(): TickResult = TickResult.ALL_DONE
    override fun whenRemove() {}
}

private data object EmptyPlayerSkillTick : PlayerSkillTick<Skill> {
    override val skill: Skill = Skill.empty()
    override val context: SkillContext = SkillContext.empty()
    override var tickCount: Long
        get() = throw UnsupportedOperationException()
        set(_) = throw UnsupportedOperationException()

    override fun isForbidden(type: SkillStateInfo.Type, trigger: SingleTrigger): Boolean = false
    override fun isInterrupted(type: SkillStateInfo.Type, trigger: SingleTrigger): Boolean = false
}

/**
 * 一个特殊的 [SkillTick] 结果. 用于玩家状态机.
 *
 * 当然它也可以与 [SkillTick] 一样使用.
 * @see tickCast
 */
interface PlayerSkillTick<S : Skill> : SkillTick<S> {
    /**
     * 此次触发效果的触发次数.
     *
     * 与下面的 tickCount 不同, 此次触发效果的触发次数是指整个过程中的触发次数.
     */
    override var tickCount: Long

    fun isForbidden(type: SkillStateInfo.Type, trigger: SingleTrigger): Boolean
    fun isInterrupted(type: SkillStateInfo.Type, trigger: SingleTrigger): Boolean

    fun tickCastPoint(tickCount: Long): TickResult = TickResult.ALL_DONE

    /**
     * 触发一次技能施法的效果.
     *
     * 此方法将会在下面两种情况时被调用:
     * 1. 当 [PlayerSkillTick] 不被玩家触发时.
     * 2. 当 [PlayerSkillTick] 被玩家触发时, 且玩家处于施法状态时.
     */
    fun tickCast(tickCount: Long): TickResult = TickResult.ALL_DONE
    fun tickBackswing(tickCount: Long): TickResult = TickResult.ALL_DONE
}

/**
 * 表示一个技能触发效果的结果.
 */
abstract class AbstractSkillTick<S : Skill>(
    final override val skill: S,
    final override val context: SkillContext
) : SkillTick<S> {
    override var tickCount: Long = 0

    protected fun checkConditions(successOperator: Boolean = true, failureOperator: Boolean = true): Boolean {
        val conditions = skill.conditions
        val session = conditions.newSession(ConditionTime.CASTING, context)
        if (successOperator && session.isSuccess) {
            session.onSuccess(context)
        }

        if (failureOperator && !session.isSuccess) {
            session.onFailure(context)
        }

        return session.isSuccess
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractPlayerSkillTick<*>) return false

        if (skill != other.skill) return false
        if (context != other.context) return false

        return true
    }

    override fun hashCode(): Int {
        var result = skill.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }

    override fun toString(): String {
        return toSimpleString()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("skill", skill)
        )
    }
}

abstract class AbstractPlayerSkillTick<S : Skill>(
    skill: S,
    context: SkillContext
) : AbstractSkillTick<S>(skill, context), PlayerSkillTick<S> {
    /**
     * 此次触发效果中不允许的触发器.
     *
     * 不允许的触发器将会在触发时被取消.
     */
    open val forbiddenTriggers: TriggerConditions = TriggerConditions.empty()

    /**
     * 此次触发效果中的中断触发器.
     */
    open val interruptTriggers:TriggerConditions = TriggerConditions.empty()

    final override fun tick(): TickResult {
        return tickCast(tickCount)
    }

    final override fun isForbidden(type: SkillStateInfo.Type, trigger: SingleTrigger): Boolean {
        return forbiddenTriggers.values.get(type).contains(trigger)
    }

    final override fun isInterrupted(type: SkillStateInfo.Type, trigger: SingleTrigger): Boolean {
        return interruptTriggers.values.get(type).contains(trigger)
    }
}