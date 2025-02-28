package cc.mewcraft.wakame.ability.state

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.ability.trigger.SingleTrigger
import cc.mewcraft.wakame.user.PlayerAdapters
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.cooldown.Cooldown
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import java.util.*
import java.util.stream.Stream
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 技能状态
 */
sealed interface AbilityState<U> : Examinable {
    val user: User<U>

    /**
     * 添加一次技能触发
     */
    fun addTrigger(trigger: SingleTrigger): AbilityStateResult

    /**
     * 将技能状态恢复为默认
     */
    fun reset()

    fun cleanup()
}

fun AbilityState(user: User<Player>): AbilityState<Player> {
    return PlayerAbilityState(user.uniqueId)
}

class PlayerAbilityState(
    private val uniqueId: UUID,
) : AbilityState<Player>, Examinable {
    companion object {
        private val COOLDOWN_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)
    }

    private val cooldown: Cooldown = Cooldown.ofTicks(2)

    private val player: Player
        get() = requireNotNull(SERVER.getPlayer(uniqueId))
    override val user: User<Player>
        get() = PlayerAdapters.get<Player>().adapt(uniqueId)

    private var stateInfo: StateInfo by AbilityStateProvider { PlayerStateInfo(player) }

    override fun addTrigger(trigger: SingleTrigger): AbilityStateResult {
        if (trigger in COOLDOWN_TRIGGERS && !cooldown.test()) {
            return AbilityStateResult.SILENT_FAILURE
        }
        return stateInfo.addTrigger(trigger)
    }

    override fun reset() {
        cooldown.reset()
    }

    override fun cleanup() {
        cooldown.reset()
        stateInfo.cleanup()
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

private class AbilityStateProvider(
    private val initializer: () -> StateInfo,
) : ReadWriteProperty<Any, StateInfo> {
    private var stateInfo: StateInfo? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): StateInfo {
        return stateInfo ?: initializer().also { stateInfo = it }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: StateInfo) {
        stateInfo = value
    }
}