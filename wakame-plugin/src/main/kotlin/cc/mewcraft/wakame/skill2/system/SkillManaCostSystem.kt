package cc.mewcraft.wakame.skill2.system

import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.ManaCostComponent
import cc.mewcraft.wakame.ecs.component.MochaEngineComponent
import cc.mewcraft.wakame.ecs.component.TickResultComponent
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.event.PlayerManaCostEvent
import cc.mewcraft.wakame.event.PlayerNoEnoughManaEvent
import cc.mewcraft.wakame.molang.ManaPenalty
import cc.mewcraft.wakame.resource.ResourceTypeRegistry
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.bindInstance
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import org.bukkit.entity.Player

class SkillManaCostSystem : IteratingSystem(
    family = family { all(CastBy, ManaCostComponent, MochaEngineComponent, TickResultComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val tickResult = entity[TickResultComponent].result
        if (tickResult != TickResult.NEXT_STATE) {
            return
        }
        val bukkitEntity = entity[CastBy].entity as? Player ?: return
        val penalty = entity[ManaCostComponent].penalty
        val engine = entity[MochaEngineComponent].mochaEngine.also {
            it.bindInstance<ManaPenalty>(ManaPenalty(penalty.penaltyCount), "mana_penalty")
        }
        val user = bukkitEntity.toUser()
        val manaCost = entity[ManaCostComponent].expression.evaluate(engine).toInt()
        if (!user.resourceMap.take(ResourceTypeRegistry.MANA, manaCost)) {
            PlayerNoEnoughManaEvent(bukkitEntity, manaCost).callEvent()
            entity[TickResultComponent].result = TickResult.RESET_STATE
        } else {
            penalty.cooldown.reset()
            PlayerManaCostEvent(bukkitEntity, manaCost).callEvent()
        }
    }
}