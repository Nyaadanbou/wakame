package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.component.AbilityTickResultComponent
import cc.mewcraft.wakame.ability.data.TickResult
import cc.mewcraft.wakame.ability.component.AbilityComponent
import cc.mewcraft.wakame.ability.component.CastBy
import cc.mewcraft.wakame.entity.resource.ResourceTypeRegistry
import cc.mewcraft.wakame.event.bukkit.PlayerManaCostEvent
import cc.mewcraft.wakame.event.bukkit.PlayerNoEnoughManaEvent
import cc.mewcraft.wakame.molang.ManaPenalty
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.bindInstance
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class AbilityManaCostSystem : IteratingSystem(
    family = family { all(AbilityComponent, CastBy, AbilityTickResultComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val tickResult = entity[AbilityTickResultComponent].result
        if (tickResult != TickResult.NEXT_STATE) {
            return
        }
        val bukkitPlayer = entity[CastBy].player()
        val penalty = entity[AbilityComponent].penalty
        val engine = entity[AbilityComponent].mochaEngine.also {
            it.bindInstance<ManaPenalty>(ManaPenalty(penalty.penaltyCount), "mana_penalty")
        }
        val user = bukkitPlayer.toUser()
        val manaCost = entity[AbilityComponent].manaCost.evaluate(engine).toInt()
        if (!user.resourceMap.take(ResourceTypeRegistry.MANA, manaCost)) {
            PlayerNoEnoughManaEvent(bukkitPlayer, manaCost).callEvent()
            entity[AbilityTickResultComponent].result = TickResult.RESET_STATE
        } else {
            penalty.cooldown.reset()
            PlayerManaCostEvent(bukkitPlayer, manaCost).callEvent()
        }
    }
}