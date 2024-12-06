package cc.mewcraft.wakame.skill2.state

import cc.mewcraft.wakame.skill2.MechanicWorldInteraction
import cc.mewcraft.wakame.skill2.trigger.SingleTrigger
import cc.mewcraft.wakame.user.PlayerAdapters
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.toSimpleString
import me.lucko.helper.cooldown.Cooldown
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import java.util.stream.Stream

/**
 * 技能状态
 */
sealed interface SkillState<U> : Examinable {
    val user: User<U>

    /**
     * 添加一次技能触发
     */
    fun addTrigger(trigger: SingleTrigger): SkillStateResult

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
    private val uniqueId: UUID,
) : SkillState<Player> {
    companion object : KoinComponent {
        private val mechanicWorldInteraction: MechanicWorldInteraction by inject()

        private val COOLDOWN_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)
    }

    private val cooldown: Cooldown = Cooldown.ofTicks(2)

    override val user: User<Player>
        get() = PlayerAdapters.get<Player>().adapt(uniqueId)

    override fun addTrigger(trigger: SingleTrigger): SkillStateResult {
        if (trigger in COOLDOWN_TRIGGERS && !cooldown.test()) {
            return SkillStateResult.SILENT_FAILURE
        }

        val results = mechanicWorldInteraction.getStateInfos(user.player).entries.map { it.value.addTrigger(trigger) }

        return if (results.any { result -> result == SkillStateResult.CANCEL_EVENT }) SkillStateResult.CANCEL_EVENT
        else if (results.any { result -> result == SkillStateResult.SILENT_FAILURE }) SkillStateResult.SILENT_FAILURE
        else SkillStateResult.SUCCESS
    }

    override fun clear() {
        cooldown.reset()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("uniqueId", uniqueId)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}