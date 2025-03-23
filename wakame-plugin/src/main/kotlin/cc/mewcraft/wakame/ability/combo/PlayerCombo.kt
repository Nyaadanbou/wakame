package cc.mewcraft.wakame.ability.combo

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.ability2.trigger.AbilitySingleTrigger
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

class PlayerCombo(
    private val uniqueId: UUID,
) : Examinable {
    companion object {
        private val COOLDOWN_TRIGGERS: List<AbilitySingleTrigger> =
            listOf(AbilitySingleTrigger.LEFT_CLICK, AbilitySingleTrigger.RIGHT_CLICK)
    }

    private val cooldown: Cooldown = Cooldown.ofTicks(2)

    private val player: Player
        get() = requireNotNull(SERVER.getPlayer(uniqueId))
    val user: User<Player>
        get() = PlayerAdapters.get<Player>().adapt(uniqueId)

    private var comboDisplay: PlayerComboInfo by ComboDisplayProvider { PlayerComboInfo(player) }

    fun addTrigger(trigger: AbilitySingleTrigger): PlayerComboResult {
        if (trigger in COOLDOWN_TRIGGERS && cooldown.test()) {
            return PlayerComboResult.SILENT_FAILURE
        }
        return comboDisplay.addTrigger(trigger)
    }

    fun reset() {
        cooldown.reset()
    }

    fun cleanup() {
        comboDisplay.cleanup()
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

private class ComboDisplayProvider(
    private val initializer: () -> PlayerComboInfo
) : ReadWriteProperty<Any, PlayerComboInfo> {
    private var stateInfo: PlayerComboInfo? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): PlayerComboInfo {
        return stateInfo ?: initializer().also { stateInfo = it }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: PlayerComboInfo) {
        stateInfo = value
    }
}