package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.component.*
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ability.data.StatePhase
import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.bridge.BukkitPlayer
import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.item.ItemSlot

object AbilityEcsBridge {

    fun createEcsEntity(
        ability: Ability, input: AbilityInput, phase: StatePhase, slot: ItemSlot?,
    ): FleksEntity = Fleks.createEntity { entity ->
        entity += AbilityArchetypeComponent(ability.archetype)
        entity += AbilityComponent(
            abilityId = ability.key,
            phase = phase,
            trigger = input.trigger,
            variant = input.variant,
            mochaEngine = input.mochaEngine
        )
        ability.configuration().invoke(this, entity)
        entity += CastBy(input.castBy.entity)
        entity += TargetTo(input.targetTo.entity)
        input.manaCost?.let { entity += ManaCost(it) }
        slot?.let { entity += AtSlot(it) }
        entity += TickCountComponent(0)
    }

    fun getPlayerAllAbilities(bukkitPlayer: BukkitPlayer): List<PlayerAbility> {
        return bukkitPlayer.koishify()[AbilityContainer].convertToPlayerAbilityList()
    }

}