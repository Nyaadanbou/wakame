package cc.mewcraft.wakame.skill2.system

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.StatePhaseComponent
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill2.component.MechanicSessionComponent
import cc.mewcraft.wakame.skill2.condition.ConditionPhase
import cc.mewcraft.wakame.util.Key
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject

class MechanicConditionSystem(
    private val wakameWorld: WakameWorld = inject()
) : IteratingSystem(
    family = family { all(IdentifierComponent, BukkitEntityComponent, StatePhaseComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val skill = SkillRegistry.INSTANCES[Key(entity[IdentifierComponent].id)]
        val phase = entity[StatePhaseComponent].phase.toConditionPhase() ?: return
        val componentMap = wakameWorld.componentMap(entity)
        val session = skill.conditions.newSession(phase, componentMap)
        entity.configure {
            it += MechanicSessionComponent(session)
        }
    }

    private fun StatePhase.toConditionPhase(): ConditionPhase? = when (this) {
        StatePhase.CAST_POINT -> ConditionPhase.CAST_POINT
        StatePhase.CASTING -> ConditionPhase.CASTING
        else -> null
    }
}