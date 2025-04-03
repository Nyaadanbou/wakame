package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.TickResult
import cc.mewcraft.wakame.ability2.component.AbilityComponent
import cc.mewcraft.wakame.ability2.component.AbilityTickResultComponent
import cc.mewcraft.wakame.ability2.component.CastBy
import cc.mewcraft.wakame.ability2.component.ManaCost
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.ecs.component.Mana
import cc.mewcraft.wakame.event.bukkit.PlayerManaConsumeEvent
import cc.mewcraft.wakame.event.bukkit.PlayerNotEnoughManaEvent
import cc.mewcraft.wakame.molang.ManaPenalty
import cc.mewcraft.wakame.util.bindInstance
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family

class AbilityManaCostSystem : IteratingSystem(
    family = family { all(AbilityComponent, CastBy, ManaCost, AbilityTickResultComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val tickResult = entity[AbilityTickResultComponent].result
        if (tickResult != TickResult.ADVANCE_TO_NEXT_STATE) {
            return
        }
        val caster = entity[CastBy].caster
        if (caster !in Families.BUKKIT_PLAYER)
            return
        val bukkitPlayer = caster[BukkitPlayerComponent].bukkitPlayer
        val mana = caster[Mana]
        val penalty = entity[ManaCost].penalty
        val engine = entity[AbilityComponent].mochaEngine.also {
            it.bindInstance<ManaPenalty>(ManaPenalty(penalty.penaltyCount), "mana_penalty")
        }
        val manaCost = entity[ManaCost].manaCost.evaluate(engine).toInt()
        if (!mana.costMana(manaCost)) {
            PlayerNotEnoughManaEvent(bukkitPlayer, manaCost).callEvent()
            entity[AbilityTickResultComponent].result = TickResult.RESET_STATE
        } else {
            penalty.resetCooldown.reset()
            PlayerManaConsumeEvent(bukkitPlayer, manaCost).callEvent()
        }
    }
}