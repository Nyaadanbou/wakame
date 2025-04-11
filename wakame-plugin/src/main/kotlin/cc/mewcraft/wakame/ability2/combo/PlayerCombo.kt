package cc.mewcraft.wakame.ability2.combo

import cc.mewcraft.wakame.ability2.trigger.AbilitySingleTrigger
import cc.mewcraft.wakame.ecs.bridge.EComponent
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.util.adventure.toSimpleString
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import java.util.*
import java.util.stream.Stream

class PlayerCombo(
    private val uniqueId: UUID,
) : Examinable, EComponent<PlayerCombo> {
    constructor(player: Player) : this(player.uniqueId)

    companion object : EComponentType<PlayerCombo>() {
        private val VALID_TRIGGERS: List<AbilitySingleTrigger> =
            listOf(AbilitySingleTrigger.LEFT_CLICK, AbilitySingleTrigger.RIGHT_CLICK)
    }

    private val comboInfo: PlayerComboInfo by lazy { PlayerComboInfo(uniqueId) }

    fun handleTrigger(trigger: AbilitySingleTrigger) {
        if (trigger in VALID_TRIGGERS) {
            comboInfo.handleTrigger(trigger)
        }
    }

    override fun World.onRemove(entity: Entity) {
        comboInfo.cleanup()
    }

    override fun type(): EComponentType<PlayerCombo> = PlayerCombo

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("uniqueId", uniqueId)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}