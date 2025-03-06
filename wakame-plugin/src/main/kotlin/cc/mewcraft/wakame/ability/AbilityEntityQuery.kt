package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.character.Caster
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ecs.ECS
import cc.mewcraft.wakame.ecs.FamilyDefinitions
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.HoldBy
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.ecs.external.KoishEntity
import cc.mewcraft.wakame.util.Identifier
import com.github.quillraven.fleks.Entity

/**
 * 用于外部快捷地获取特定技能.
 */
object AbilityEntityQuery {
    fun createAbilityEntity(ability: Ability, input: AbilityInput): Entity {
        return ECS.createEntity(ability.archetype.key) {
            it += AbilityComponent(
                abilityId = ability.key,
                manaCost = input.manaCost,
                phase = StatePhase.IDLE,
                trigger = input.trigger,
                variant = input.variant,
                mochaEngine = input.mochaEngine
            )
            ability.configuration().invoke(this, it)
            it += Tags.DISPOSABLE
            it += CastBy(input.castBy)
            it += TargetTo(input.targetTo)
            HoldBy(input.holdBy)?.let { holdBy -> it += holdBy }
            input.holdBy?.let { castItem -> it += HoldBy(slot = castItem.first, nekoStack = castItem.second.clone()) }
            it += TickCountComponent(.0)
        }
    }

    fun findAbilityComponentBridges(abilityId: Identifier, caster: Caster): List<KoishEntity> {
        val koishEntities = mutableListOf<KoishEntity>()
        FamilyDefinitions.ABILITY.forEach { entity ->
            if (entity[AbilityComponent.Companion].abilityId != abilityId)
                return@forEach
            if (entity[CastBy.Companion].caster != caster)
                return@forEach
            koishEntities.add(KoishEntity(entity))
        }
        return koishEntities
    }

    fun findAbilities(abilityId: Identifier, caster: Caster): List<PlayerAbility> {
        val componentBridges = findAbilityComponentBridges(abilityId, caster)
        return componentBridges.map { it.getPlayerAbility() }
    }

    fun findAllAbilities(caster: Caster): List<PlayerAbility> {
        val koishEntities = mutableListOf<KoishEntity>()
        FamilyDefinitions.ABILITY.forEach { entity ->
            if (entity[CastBy.Companion].caster != caster)
                return@forEach
            koishEntities.add(KoishEntity(entity))
        }

        return koishEntities.map { it.getPlayerAbility() }
    }

    fun editAbilities(abilityId: Identifier, caster: Caster, block: (KoishEntity) -> Unit) {
        val componentBridges = findAbilityComponentBridges(abilityId, caster)
        for (bridge in componentBridges) {
            block(bridge)
        }
    }

    private fun KoishEntity.getPlayerAbility(): PlayerAbility {
        val abilityComponent = get(AbilityComponent.Companion)
        return PlayerAbility(
            id = abilityComponent.abilityId,
            trigger = abilityComponent.trigger,
            variant = abilityComponent.variant,
            manaCost = abilityComponent.manaCost,
        )
    }
}