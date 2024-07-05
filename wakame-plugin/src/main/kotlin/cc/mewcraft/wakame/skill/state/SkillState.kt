package cc.mewcraft.wakame.skill.state

import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.user.User
import me.lucko.helper.cooldown.Cooldown
import org.bukkit.entity.Player

/**
 * 技能状态
 */
interface SkillState {
    /**
     * 技能状态信息
     */
    val info: SkillStateInfo

    /**
     * 添加一次技能触发
     */
    fun addTrigger(trigger: SingleTrigger, skillContext: SkillContext): SkillStateResult

    /**
     * 刷新一次技能状态
     */
    fun tick()

    /**
     * 中断技能状态
     */
    fun interrupt()

    /**
     * 将技能状态恢复为默认
     */
    fun clear()
}

class PlayerSkillState(
    val user: User<Player>
) : SkillState {
    companion object {
        private val COOLDOWN_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)
    }

    private val cooldown: Cooldown = Cooldown.ofTicks(2)
    override var info: SkillStateInfo = IdleStateInfo(this)
        private set

    override fun addTrigger(trigger: SingleTrigger, skillContext: SkillContext): SkillStateResult {
        if (trigger in COOLDOWN_TRIGGERS && !cooldown.test()) {
            return SkillStateResult.SILENT_FAILURE
        }

        val result = info.addTrigger(trigger, skillContext)
        return result
    }

    override fun tick() {
        info.tick()
    }

    override fun interrupt() {
        info.interrupt()
    }

    override fun clear() {
        info.interrupt()
        cooldown.reset()
    }

    fun setInfo(skillStateInfo: SkillStateInfo) {
        user.player.sendPlainMessage("技能状态变更: $info -> $skillStateInfo")
        this.info = skillStateInfo
    }
}