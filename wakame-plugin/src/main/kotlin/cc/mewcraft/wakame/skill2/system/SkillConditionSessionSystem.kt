package cc.mewcraft.wakame.skill2.system

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.EntityType
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.skill2.component.SkillSessionComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject

class SkillConditionSessionSystem(
    private val wakameWorld: WakameWorld = inject()
) : IteratingSystem(
    family = family { all(SkillSessionComponent, EntityType.SKILL) }
) {
    override fun onTickEntity(entity: Entity) {
        val session = entity[SkillSessionComponent].session
        val componentMap = wakameWorld.componentMap(entity)

        if (session.isSuccess) {
            session.onSuccess(componentMap)
        } else {
            entity.configure {
                it -= Tags.CAN_TICK
            }
            session.onFailure(componentMap)
        }
    }
}