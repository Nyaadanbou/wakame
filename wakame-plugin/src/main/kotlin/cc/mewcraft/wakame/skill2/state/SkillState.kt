package cc.mewcraft.wakame.skill2.state

import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.event.PlayerSkillStateChangeEvent
import cc.mewcraft.wakame.skill2.trigger.SingleTrigger
import cc.mewcraft.wakame.user.PlayerAdapters
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.toSimpleString
import me.lucko.helper.cooldown.Cooldown
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.Server
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*
import java.util.stream.Stream
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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
        private val server: Server by inject()

        private val COOLDOWN_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)
    }

    private val cooldown: Cooldown = Cooldown.ofTicks(2)

    private val player: Player
        get() = requireNotNull(server.getPlayer(uniqueId))
    override val user: User<Player>
        get() = PlayerAdapters.get<Player>().adapt(uniqueId)

    private var stateInfo: StateInfo by SkillStateProvider { createStateInfo(player, StatePhase.IDLE) }

    private fun createStateInfo(player: Player, phase: StatePhase): StateInfo {
        return when (phase) {
            StatePhase.IDLE -> IdleStateInfo(player)
            StatePhase.CAST_POINT -> CastPointStateInfo(player)
            StatePhase.CASTING -> CastingStateInfo(player)
            StatePhase.BACKSWING -> BackswingStateInfo(player)
        }
    }

    fun onStateChange(event: PlayerSkillStateChangeEvent) {
        stateInfo = createStateInfo(event.player, event.newPhase)
    }

    override fun addTrigger(trigger: SingleTrigger): SkillStateResult {
        if (stateInfo.phase != StatePhase.IDLE) {
            return SkillStateResult.SILENT_FAILURE
        }
        if (trigger in COOLDOWN_TRIGGERS && !cooldown.test()) {
            return SkillStateResult.SILENT_FAILURE
        }
        return stateInfo.addTrigger(trigger)
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

private class SkillStateProvider(
    private val initializer: () -> StateInfo
) : ReadWriteProperty<Any, StateInfo> {
    private var stateInfo: StateInfo? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): StateInfo {
        return stateInfo ?: initializer().also { stateInfo = it }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: StateInfo) {
        stateInfo = value
    }
}