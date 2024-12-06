package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.StateInfoComponent
import cc.mewcraft.wakame.ecs.component.StatePhaseComponent
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.skill2.state.BackswingStateInfo
import cc.mewcraft.wakame.skill2.state.CastPointStateInfo
import cc.mewcraft.wakame.skill2.state.CastingStateInfo
import cc.mewcraft.wakame.skill2.state.IdleStateInfo
import cc.mewcraft.wakame.skill2.state.StateInfo
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import org.bukkit.entity.Player

class StateInfoSystem : IteratingSystem(
    family = family { all(BukkitEntityComponent, StatePhaseComponent) }
){
    override fun onTickEntity(entity: Entity) {
        val statePhase = entity[StatePhaseComponent].phase
        val player = entity[BukkitEntityComponent].entity as? Player ?: return

        val stateInfo = createStateInfo(player, statePhase)

        if (entity.hasNo(StateInfoComponent)) {
            entity.configure { it += StateInfoComponent(stateInfo) }
        } else {
            entity[StateInfoComponent].stateInfo = stateInfo
        }
    }


    private fun createStateInfo(player: Player, phase: StatePhase): StateInfo {
        return when (phase) {
            StatePhase.IDLE -> IdleStateInfo(player)
            StatePhase.CAST_POINT -> CastPointStateInfo(player)
            StatePhase.CASTING -> CastingStateInfo(player)
            StatePhase.BACKSWING -> BackswingStateInfo(player)
        }
    }
}