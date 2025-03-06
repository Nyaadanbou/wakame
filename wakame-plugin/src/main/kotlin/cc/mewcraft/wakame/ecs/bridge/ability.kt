package cc.mewcraft.wakame.ecs.bridge

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.PlayerAbility
import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ecs.ECS
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.HoldBy
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.component.WithAbility
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.ecs.external.KoishEntity
import com.github.quillraven.fleks.Entity

fun Ability.createAbilityEntity(input: AbilityInput): Entity {
    return ECS.createEntity(archetype.key) {
        it += AbilityComponent(
            abilityId = key,
            manaCost = input.manaCost,
            phase = StatePhase.IDLE,
            trigger = input.trigger,
            variant = input.variant,
            mochaEngine = input.mochaEngine
        )
        configuration().invoke(this, it)
        it += Tags.DISPOSABLE
        it += CastBy(input.castBy)
        it += TargetTo(input.targetTo)
        HoldBy(input.holdBy)?.let { holdBy -> it += holdBy }
        input.holdBy?.let { castItem -> it += HoldBy(slot = castItem.first, nekoStack = castItem.second.clone()) }
        it += TickCountComponent(.0)
    }
}

fun BukkitPlayer.findKoishAbilityEntity(archetype: AbilityArchetype): Collection<KoishEntity> {
    return toKoish()[WithAbility].abilities.get(archetype).map { KoishEntity(it) }
}

fun BukkitPlayer.findAbilities(archetype: AbilityArchetype): List<PlayerAbility> {
    val componentBridges = findKoishAbilityEntity(archetype)
    return componentBridges.map { it.getPlayerAbility() }
}

fun BukkitPlayer.findAllAbilities(): List<PlayerAbility> {
    val koishEntities = toKoish()[WithAbility].abilities.values()
    return koishEntities.map { KoishEntity(it).getPlayerAbility() }
}

fun BukkitPlayer.editAbilities(archetype: AbilityArchetype, block: (KoishEntity) -> Unit) {
    val componentBridges = findKoishAbilityEntity(archetype)
    for (bridge in componentBridges) {
        block(bridge)
    }
}

fun BukkitPlayer.cleanupAbility() {
    val abilities = toKoish()[WithAbility].abilities.values().iterator()
    while (abilities.hasNext()) {
        val abilityEntity = abilities.next()
        ECS.removeEntity(abilityEntity)
        abilities.remove()
    }
}

fun KoishEntity.getPlayerAbility(): PlayerAbility {
    val abilityComponent = get(AbilityComponent)
    return PlayerAbility(
        id = abilityComponent.abilityId,
        trigger = abilityComponent.trigger,
        variant = abilityComponent.variant,
        manaCost = abilityComponent.manaCost,
    )
}