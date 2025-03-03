package cc.mewcraft.wakame.ecs.external

import cc.mewcraft.wakame.ability.PlayerAbility
import cc.mewcraft.wakame.ability.character.Caster
import cc.mewcraft.wakame.ecs.FamilyDefinitions
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.util.Identifier

/**
 * 用于外部快捷地获取特定技能.
 */
object AbilityEntityQuery {
    fun findAbilityComponentBridges(id: Identifier, caster: Caster): List<ComponentBridge> {
        val componentBridges = mutableListOf<ComponentBridge>()
        FamilyDefinitions.ABILITY.forEach { entity ->
            if (entity[IdentifierComponent].id != id)
                return@forEach
            if (entity[CastBy].caster != caster)
                return@forEach
            componentBridges.add(ComponentBridge(entity))
        }
        return componentBridges
    }

    fun findAbilities(id: Identifier, caster: Caster): List<PlayerAbility> {
        val componentBridges = findAbilityComponentBridges(id, caster)
        return componentBridges.map { it.getPlayerAbility() }
    }

    fun findAllAbilities(caster: Caster): List<PlayerAbility> {
        val componentBridges = mutableListOf<ComponentBridge>()
        FamilyDefinitions.ABILITY.forEach { entity ->
            if (entity[CastBy].caster != caster)
                return@forEach
            componentBridges.add(ComponentBridge(entity))
        }

        return componentBridges.map { it.getPlayerAbility() }
    }

    fun editAbilities(id: Identifier, caster: Caster, block: (ComponentBridge) -> Unit) {
        val componentBridges = findAbilityComponentBridges(id, caster)
        for (bridge in componentBridges) {
            block(bridge)
        }
    }

    private fun ComponentBridge.getPlayerAbility(): PlayerAbility {
        val id = getOrThrow(IdentifierComponent).id
        val abilityComponent = getOrThrow(AbilityComponent)
        return PlayerAbility(
            id = id,
            trigger = abilityComponent.trigger,
            variant = abilityComponent.variant,
            manaCost = abilityComponent.manaCost,
        )
    }
}