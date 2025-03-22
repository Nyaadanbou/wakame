package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.component.AbilityArchetypeComponent
import cc.mewcraft.wakame.ability.component.AbilityComponent
import cc.mewcraft.wakame.ability.component.AbilityContainer
import cc.mewcraft.wakame.ability.component.AtSlot
import cc.mewcraft.wakame.ability.component.CastBy
import cc.mewcraft.wakame.ability.component.ManaCost
import cc.mewcraft.wakame.ability.component.TargetTo
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ability.data.StatePhase
import cc.mewcraft.wakame.ecs.Fleks
import cc.mewcraft.wakame.ecs.bridge.BukkitPlayer
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.molang.Evaluable
import com.github.quillraven.fleks.Entity

fun Ability.createAbilityEntity(
    input: AbilityInput,
    phase: StatePhase,
    slot: ItemSlot?,
): Entity {
    return Fleks.createEntity {
        it += AbilityArchetypeComponent(archetype)
        it += AbilityComponent(
            abilityId = key,
            phase = phase,
            trigger = input.trigger,
            variant = input.variant,
            mochaEngine = input.mochaEngine
        )
        configuration().invoke(this, it)
        it += CastBy(input.castBy.entity)
        it += TargetTo(input.targetTo.entity)
        slot?.let { slot -> it += AtSlot(slot) }
        it += TickCountComponent(0)
    }
}

fun BukkitPlayer.findKoishAbilityEntity(archetype: AbilityArchetype): Collection<KoishEntity> {
    return koishify()[AbilityContainer][archetype].map(::KoishEntity)
}

fun BukkitPlayer.findAbilities(archetype: AbilityArchetype): List<PlayerAbility> {
    val componentBridges = findKoishAbilityEntity(archetype)
    return componentBridges.map { it.getPlayerAbility() }
}

fun BukkitPlayer.findAllAbilities(): List<PlayerAbility> {
    val koishEntities = koishify()[AbilityContainer].values
    return koishEntities.map { KoishEntity(it).getPlayerAbility() }
}

fun BukkitPlayer.editAbilities(archetype: AbilityArchetype, block: (KoishEntity) -> Unit) {
    val componentBridges = findKoishAbilityEntity(archetype)
    for (bridge in componentBridges) {
        block(bridge)
    }
}

fun KoishEntity.getPlayerAbility(): PlayerAbility {
    val abilityComponent = get(AbilityComponent)
    val manaCost = getOrNull(ManaCost)?.manaCost ?: Evaluable.parseNumber(0)
    return PlayerAbility(
        id = abilityComponent.abilityId,
        trigger = abilityComponent.trigger,
        variant = abilityComponent.variant,
        manaCost = manaCost,
    )
}