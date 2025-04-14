package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.StatePhase
import cc.mewcraft.wakame.ability2.component.Ability
import cc.mewcraft.wakame.ability2.component.CastBy
import cc.mewcraft.wakame.ability2.component.ManaCost
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.ecs.component.Mana
import cc.mewcraft.wakame.event.bukkit.PlayerManaConsumeEvent
import cc.mewcraft.wakame.event.bukkit.PlayerNotEnoughManaEvent
import cc.mewcraft.wakame.molang.ManaPenalty
import cc.mewcraft.wakame.util.bindInstance
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

object ConsumeManaForAbilities : IteratingSystem(
    family = EWorld.family { all(Ability, CastBy, ManaCost) }
) {
    override fun onTickEntity(entity: Entity) {
        val caster = entity[CastBy].caster
        if (caster !in Families.BUKKIT_PLAYER)
            return
        val player = caster[BukkitPlayer].unwrap()
        val mana = caster[Mana]
        val penalty = entity[ManaCost].penalty
        val engine = entity[Ability].mochaEngine.also {
            it.bindInstance<ManaPenalty>(ManaPenalty(penalty.penaltyCount), "mana_penalty")
        }
        val manaCost = entity[ManaCost].manaCost.evaluate(engine).toInt()
        if (!mana.costMana(manaCost)) {
            PlayerNotEnoughManaEvent(player, manaCost).callEvent()
            entity[Ability].phase = StatePhase.RESET
        } else {
            penalty.resetCooldown.reset()
            PlayerManaConsumeEvent(player, manaCost).callEvent()
        }
    }
}