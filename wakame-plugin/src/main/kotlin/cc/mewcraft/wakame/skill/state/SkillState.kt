package cc.mewcraft.wakame.skill.state

import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.user.User
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent

/**
 * 技能状态
 */
interface SkillState {

    val info: SkillStateInfo

    /**
     * 添加一次技能触发
     */
    fun addTrigger(trigger: SingleTrigger, skillCastContext: SkillCastContext): SkillStateResult

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
) : SkillState, KoinComponent {
    override var info: SkillStateInfo = SkillStateInfo.idle(this)
        private set

    override fun addTrigger(trigger: SingleTrigger, skillCastContext: SkillCastContext): SkillStateResult {
        return info.addTrigger(trigger, skillCastContext)
    }

    override fun tick() {
        info.tick()
    }

    override fun interrupt() {
        info.interrupt()
    }

    override fun clear() {
        setInfo(SkillStateInfo.idle(this))
    }

    fun setInfo(skillStateInfo: SkillStateInfo) {
        this.info = (skillStateInfo)
    }
}