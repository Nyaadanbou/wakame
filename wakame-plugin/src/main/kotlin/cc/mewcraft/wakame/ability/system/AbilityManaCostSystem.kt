package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.component.AbilityComponent
import cc.mewcraft.wakame.ability.component.AbilityTickResultComponent
import cc.mewcraft.wakame.ability.component.CastBy
import cc.mewcraft.wakame.ability.component.ManaCost
import cc.mewcraft.wakame.ability.data.TickResult
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.entity.resource.ResourceTypeRegistry
import cc.mewcraft.wakame.event.bukkit.PlayerManaConsumeEvent
import cc.mewcraft.wakame.event.bukkit.PlayerNotEnoughManaEvent
import cc.mewcraft.wakame.molang.ManaPenalty
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.bindInstance
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class AbilityManaCostSystem : IteratingSystem(
    family = family { all(AbilityComponent, CastBy, ManaCost, AbilityTickResultComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val tickResult = entity[AbilityTickResultComponent].result
        if (tickResult != TickResult.ADVANCE_NEXT_STATE) {
            return
        }
        val caster = entity[CastBy].caster
        if (caster !in Families.BUKKIT_PLAYER)
            return
        val bukkitPlayer = caster[BukkitPlayerComponent].bukkitPlayer
        val penalty = entity[ManaCost].penalty
        val engine = entity[AbilityComponent].mochaEngine.also {
            it.bindInstance<ManaPenalty>(ManaPenalty(penalty.penaltyCount), "mana_penalty")
        }
        val user = bukkitPlayer.toUser()
        val manaCost = entity[ManaCost].manaCost.evaluate(engine).toInt()
        if (!user.resourceMap.take(ResourceTypeRegistry.MANA, manaCost)) {
            PlayerNotEnoughManaEvent(bukkitPlayer, manaCost).callEvent()
            entity[AbilityTickResultComponent].result = TickResult.RESET_STATE
        } else {
            penalty.cooldown.reset()
            PlayerManaConsumeEvent(bukkitPlayer, manaCost).callEvent()
        }
    }
}