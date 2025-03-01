package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TickResultComponent
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.event.bukkit.PlayerAbilityStateChangeEvent
import cc.mewcraft.wakame.util.serverTick
import cc.mewcraft.wakame.util.text.mini
import cc.mewcraft.wakame.event.AbilityStateChangeEvent
import cc.mewcraft.wakame.registry2.KoishRegistries
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Bukkit

class StatePhaseSystem : IteratingSystem(
    family = family { all(AbilityComponent, IdentifierComponent, TickResultComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val id = entity[IdentifierComponent].id
        val tickResult = entity[TickResultComponent].result
        val oldPhase = entity[AbilityComponent].phase
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

        entity[AbilityComponent].phase = newPhase
        onStateChange(id, KoishRegistries.ABILITY.getOrThrow(id), oldPhase, newPhase)
        entity.configure { it -= Tags.NEXT_STATE }
    }

    private fun onStateChange(identifier: String, ability: Ability, old: StatePhase, new: StatePhase) {
        AbilityStateChangeEvent(ability, old, new).callEvent()
        Bukkit.broadcast("技能 $identifier 状态已切换为 $new".mini.hoverEvent(HoverEvent.showText("技能状态变更: $old -> $new. Tick: $serverTick".mini)))
    }
}