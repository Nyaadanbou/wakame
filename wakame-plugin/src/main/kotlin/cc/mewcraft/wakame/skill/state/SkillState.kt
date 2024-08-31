package cc.mewcraft.wakame.skill.state

import cc.mewcraft.wakame.event.PlayerSkillStateChangeEvent
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.user.PlayerAdapters
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.toSimpleString
import me.lucko.helper.cooldown.Cooldown
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.util.UUID
import java.util.stream.Stream

/**
 * 技能状态
 */
sealed interface SkillState<U> : Examinable {
    val user: User<U>

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

fun SkillState(user: User<Player>): SkillState<Player> {
    return PlayerSkillState(user.uniqueId)
}

// TODO 进一步封装 PlayerSkillState
class PlayerSkillState(
    private val uniqueId: UUID
) : SkillState<Player> {
    companion object {
        private val COOLDOWN_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)
    }

    private val cooldown: Cooldown = Cooldown.ofTicks(2)

    override val user: User<Player>
        get() = PlayerAdapters.get<Player>().adapt(uniqueId)

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
        try {
            info.tick()
        } catch (t: Throwable) {
            SkillStateSupport.logger.error("玩家 ($user) 的技能状态 ${info.javaClass.simpleName} 执行时发生异常", t)
            runCatching { info.interrupt() }.onFailure { info = IdleStateInfo(this) }
        }
    }

    override fun interrupt() {
        info.interrupt()
    }

    override fun clear() {
        info.interrupt()
        cooldown.reset()
    }

    fun setInfo(skillStateInfo: SkillStateInfo) {
        PlayerSkillStateChangeEvent(user.player, info, skillStateInfo).callEvent()
        user.player.sendMessage("技能状态已切换为 ${skillStateInfo.javaClass.simpleName}".mini.hoverEvent(HoverEvent.showText("技能状态变更: $info -> $skillStateInfo".mini)))
        this.info = skillStateInfo
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("info", info)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

private object SkillStateSupport : KoinComponent {
    val logger: Logger by inject()
}