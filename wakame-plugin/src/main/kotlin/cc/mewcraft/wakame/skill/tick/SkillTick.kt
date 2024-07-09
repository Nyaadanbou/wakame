package cc.mewcraft.wakame.skill.tick

import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.state.*
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.tick.Tickable
import cc.mewcraft.wakame.user.toUser

/**
 * 表示技能触发的效果.
 */
interface SkillTick : Tickable {

    companion object {
        /**
         * 一个空的技能触发效果.
         */
        fun empty(): SkillTick = EmptySkillTick
    }

    /**
     * 此次 Tick 的技能
     */
    val skill: Skill

    /**
     * 此次触发效果的上下文.
     */
    val context: SkillContext

    override fun tick(): TickResult = TickResult.ALL_DONE
}

private data object EmptySkillTick : SkillTick {
    override val skill: Skill = Skill.empty()
    override val context: SkillContext = SkillContext.empty()
}

/**
 * 一个特殊的 [SkillTick] 结果. 用于玩家状态机.
 *
 * 当然它也可以与 [SkillTick] 一样使用.
 * @see tickCast
 */
interface PlayerSkillTick : SkillTick {
    fun isForbidden(type: SkillStateInfo.Type, trigger: SingleTrigger): Boolean
    fun isInterrupted(type: SkillStateInfo.Type, trigger: SingleTrigger): Boolean

    fun tickCastPoint(): TickResult = TickResult.ALL_DONE

    /**
     * 触发一次技能施法的效果.
     *
     * 此方法将会在下面两种情况时被调用:
     * 1. 当 [PlayerSkillTick] 不被玩家触发时.
     * 2. 当 [PlayerSkillTick] 被玩家触发时, 且玩家处于施法状态时.
     */
    fun tickCast(): TickResult = TickResult.ALL_DONE
    fun tickBackswing(): TickResult = TickResult.ALL_DONE
}

/**
 * 表示一个技能触发效果的结果.
 */
abstract class AbstractSkillTick(
    final override val skill: Skill,
    final override val context: SkillContext
) : SkillTick {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractPlayerSkillTick) return false

        if (skill != other.skill) return false
        if (context != other.context) return false

        return true
    }

    override fun hashCode(): Int {
        var result = skill.hashCode()
        result = 31 * result + context.hashCode()
        return result
    }
}

abstract class AbstractPlayerSkillTick(
    skill: Skill,
    context: SkillContext
) : AbstractSkillTick(skill, context), PlayerSkillTick {
    /**
     * 此次触发效果中不允许的触发器.
     *
     * 不允许的触发器将会在触发时被取消.
     */
    open val forbiddenTriggers: TriggerConditions = TriggerConditions.empty()

    /**
     * 此次触发效果中的中断触发器.
     */
    open val interruptTriggers: TriggerConditions = TriggerConditions.empty()

    protected val userState: SkillState?
        get() {
            val user = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer?.toUser() ?: return null
            val state = user.skillState
            if (state.info.skillTick != this)
                return null
            return state
        }

    final override fun tick(): TickResult {
        val state = userState ?: return tickCast()

        return when (state.info) {
            is CastPointStateInfo -> tickCastPoint()
            is CastStateInfo -> tickCast()
            is BackswingStateInfo -> tickBackswing()
            else -> TickResult.ALL_DONE
        }
    }

    final override fun isForbidden(type: SkillStateInfo.Type, trigger: SingleTrigger): Boolean {
        return forbiddenTriggers.values.get(type).contains(trigger)
    }

    final override fun isInterrupted(type: SkillStateInfo.Type, trigger: SingleTrigger): Boolean {
        return interruptTriggers.values.get(type).contains(trigger)
    }
}