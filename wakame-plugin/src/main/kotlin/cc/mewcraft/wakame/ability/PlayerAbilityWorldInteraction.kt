package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.ecs.FamilyDefinitions
import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.registry2.KoishRegistries
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
    fun Player.getAbilityBy(trigger: Trigger): List<Ability> {
        val abilities = mutableListOf<Ability>()
        FamilyDefinitions.CASTER_ABILITY.forEach { entity ->
            if (entity[CastBy].caster.entity != this@getAbilityBy)
                return@forEach
            if (entity[AbilityComponent].phase != StatePhase.IDLE)
                // 只有在 IDLE 状态下才能进行下一个状态的标记.
                return@forEach
            if (entity[AbilityComponent].trigger == trigger) {
                val id = entity[IdentifierComponent].id
                abilities.add(KoishRegistries.ABILITY.getOrThrow(id))
            }
        }

        return abilities
    }

    fun Player.getAllActiveAbilityTriggers(): Set<Trigger> {
        val triggers = mutableSetOf<Trigger>()
        FamilyDefinitions.CASTER_ABILITY.forEach { entity ->
            if (entity[CastBy].caster.entity != this@getAllActiveAbilityTriggers)
                return@forEach
            entity[AbilityComponent].trigger?.let { triggers.add(it) }
        }

        return triggers
    }

    fun Player.setNextState(ability: Ability) {
        WakameWorld.editEntities(FamilyDefinitions.CASTER_ABILITY) { entity ->
            if (entity[CastBy].caster.entity != this@setNextState)
                return@editEntities
            if (entity[AbilityComponent].phase != StatePhase.IDLE)
                // 只有在 IDLE 状态下才能进行下一个状态的标记.
                return@editEntities
            if (entity[IdentifierComponent].id != ability.key.asString())
                return@editEntities
            entity.configure { it += Tags.NEXT_STATE }
        }
    }

    fun Player.setCostPenalty(abilityId: String, penalty: ManaCostPenalty) {
        WakameWorld.editEntities(FamilyDefinitions.CASTER_ABILITY) { entity ->
            if (entity[CastBy].caster.entity != this@setCostPenalty)
                return@editEntities
            if (entity[IdentifierComponent].id != abilityId)
                return@editEntities
            entity[AbilityComponent].penalty = penalty
        }
    }

    fun Player.cleanupAbility() {
        WakameWorld.editEntities(FamilyDefinitions.CASTER_ABILITY) { entity ->
            if (entity[CastBy].caster.entity != this@cleanupAbility)
                return@editEntities
            WakameWorld.removeEntity(entity)
        }
    }
}