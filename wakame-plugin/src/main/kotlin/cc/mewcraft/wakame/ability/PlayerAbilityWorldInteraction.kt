package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.character.CasterAdapter
import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.ecs.FamilyDefinitions
import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.ecs.external.AbilityEntityQuery
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent

internal inline fun <T> playerAbilityWorldInteraction(block: PlayerAbilityWorldInteraction.() -> T): T {
    return PlayerAbilityWorldInteraction.block()
}

@DslMarker
internal annotation class PlayerAbilityWorldInteractionDsl

/**
 * 技能与 ECS 系统交互的工具类.
 */
@PlayerAbilityWorldInteractionDsl
internal object PlayerAbilityWorldInteraction : KoinComponent {
    fun Player.getAbilitiesBy(trigger: Trigger): List<Ability> {
        return AbilityEntityQuery.findAllAbilities(CasterAdapter.adapt(this))
            .filter { it.trigger == trigger }
            .map { it.instance }
    }

    fun Player.getAllActiveAbilityTriggers(): Set<Trigger> {
        return AbilityEntityQuery.findAllAbilities(CasterAdapter.adapt(this))
            .mapNotNull { it.trigger }
            .toSet()
    }

    fun Player.setNextState(ability: Ability) {
        WakameWorld.editEntities(FamilyDefinitions.ABILITY) { entity ->
            if (entity[CastBy].caster.entity != this@setNextState)
                return@editEntities
            if (entity[AbilityComponent].phase != StatePhase.IDLE)
                // 只有在 IDLE 状态下才能进行下一个状态的标记.
                return@editEntities
            if (entity[IdentifierComponent].id != ability.archetype.key)
                return@editEntities
            entity.configure { it += Tags.NEXT_STATE }
        }
    }

    fun Player.setCostPenalty(ability: Ability, penalty: ManaCostPenalty) {
        WakameWorld.editEntities(FamilyDefinitions.ABILITY) { entity ->
            if (entity[CastBy].caster.entity != this@setCostPenalty)
                return@editEntities
            if (entity[IdentifierComponent].id != ability.archetype.key)
                return@editEntities
            entity[AbilityComponent].penalty = penalty
        }
    }

    fun Player.cleanupAbility() {
        WakameWorld.editEntities(FamilyDefinitions.ABILITY) { entity ->
            if (entity[CastBy].caster.entity != this@cleanupAbility)
                return@editEntities
            WakameWorld.removeEntity(entity)
        }
    }
}