package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
import cc.mewcraft.wakame.skill.state.BackswingStateInfo
import cc.mewcraft.wakame.skill.state.CastPointStateInfo
import cc.mewcraft.wakame.skill.state.CastStateInfo
import cc.mewcraft.wakame.user.toUser

/**
 * 表示技能触发的效果.
 */
interface SkillTick {

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
    val context: SkillCastContext

    /**
     * 此次触发效果中不允许的触发器.
     *
     * 不允许的触发器将会在触发时被取消.
     */
    val forbiddenTriggers: TriggerConditions

    /**
     * 此次触发效果中的中断触发器.
     */
    val interruptTriggers: TriggerConditions

    /**
     * 触发一 tick 的效果.
     */
    fun tick(): TickResult = TickResult.ALL_DONE
}

private data object EmptySkillTick : SkillTick {
    override val skill: Skill = Skill.empty()
    override val context: SkillCastContext = SkillCastContext.empty()
    override val forbiddenTriggers: TriggerConditions = TriggerConditions.empty()
    override val interruptTriggers: TriggerConditions = TriggerConditions.empty()
}

/**
 * 玩家技能的触发效果.
 */
abstract class PlayerSkillTick(
    final override val skill: Skill,
    final override val context: SkillCastContext
) : SkillTick {
    final override fun tick(): TickResult {
        val user = context.get(SkillCastContextKey.CASTER_PLAYER).bukkitPlayer.toUser()
        val state = user.skillState

        return when (state.info) {
            is CastPointStateInfo -> tickCastPoint()
            is CastStateInfo -> tickCast()
            is BackswingStateInfo -> tickBackswing()
            else -> TickResult.CONTINUE_TICK
        }
    }

    protected open fun tickCastPoint(): TickResult = TickResult.ALL_DONE
    protected open fun tickCast(): TickResult = TickResult.ALL_DONE
    protected open fun tickBackswing(): TickResult = TickResult.ALL_DONE
}