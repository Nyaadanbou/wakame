package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.StatePhaseComponent
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TickResultComponent
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.event.PlayerAbilityStateChangeEvent
import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.util.Key
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.entity.Player

class StatePhaseSystem(
    private val plugin: WakamePlugin = inject()
) : IteratingSystem(
    family = family { all(IdentifierComponent, CastBy, StatePhaseComponent, TickResultComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val id = entity[IdentifierComponent].id
        val tickResult = entity[TickResultComponent].result
        val oldPhase = entity[StatePhaseComponent].phase
        val newPhase = when {
            tickResult.isNextState() || entity.has(Tags.NEXT_STATE) -> {
                oldPhase.next()
            }
            tickResult == TickResult.RESET_STATE -> {
                StatePhase.RESET
            }
            else -> {
                oldPhase
            }
        }
        if (oldPhase == newPhase)
            return

        entity[StatePhaseComponent].phase = newPhase
        val player = entity[CastBy].entity as? Player ?: return
        onStateChange(id, player, AbilityRegistry.INSTANCES[Key(id)], oldPhase, newPhase)
        entity.configure { it -= Tags.NEXT_STATE }
    }

    private fun onStateChange(identifier: String, player: Player, ability: Ability, old: StatePhase, new: StatePhase) {
        PlayerAbilityStateChangeEvent(player, ability, old, new).callEvent()
        player.sendMessage("技能 $identifier 状态已切换为 $new".mini.hoverEvent(HoverEvent.showText("技能状态变更: $old -> $new. Tick: ${plugin.server.currentTick}".mini)))
    }
}