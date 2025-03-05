package cc.mewcraft.wakame.ecs.external

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.PlayerAbility
import cc.mewcraft.wakame.ability.character.Caster
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ecs.FamilyDefinitions
import cc.mewcraft.wakame.ecs.MetadataKeys
import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.WakameWorld.removeEntity
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.BlockComponent
import cc.mewcraft.wakame.ecs.component.BukkitBridgeComponent
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.HoldBy
import cc.mewcraft.wakame.ecs.component.PlayerComponent
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.component.WithAbility
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.ecs.eEntity
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.metadata.Metadata
import com.github.quillraven.fleks.Entity
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.entity.Entity as BukkitEntity

object PlayerEntityQuery {
    fun createPlayerEntity(player: Player) {
        WakameWorld.createEntity {
            it += PlayerComponent(player)
            it += BukkitBridgeComponent(MetadataKeys.PLAYER_ENTITY) { componentBridge -> Metadata.provide(componentBridge[PlayerComponent].player) }
            it += WithAbility()
        }
    }

    fun removePlayerEntity(player: Player) {
        val entityToRemove = player.eEntity().entity
        removeEntity(entityToRemove)
    }
}

object BukkitEntityEntityQuery {
    fun createBukkitEntity(bukkitEntity: BukkitEntity) {
        if (bukkitEntity is Player) {
            PlayerEntityQuery.createPlayerEntity(bukkitEntity)
            return
        }
        WakameWorld.createEntity {
            it += BukkitEntityComponent(bukkitEntity)
            it += BukkitBridgeComponent(MetadataKeys.BUKKIT_ENTITY_ENTITY) { componentBridge -> Metadata.provide(componentBridge[BukkitEntityComponent].bukkitEntity) }
            it += WithAbility()
        }
    }

    fun removeBukkitEntityEntity(bukkitEntity: BukkitEntity) {
        if (bukkitEntity is Player) {
            PlayerEntityQuery.removePlayerEntity(bukkitEntity)
            return
        }
        val entityToRemove = bukkitEntity.eEntity()?.entity ?: return
        removeEntity(entityToRemove)
    }
}

object BlockEntityQuery {
    fun createBlockEntity(block: Block) {
        WakameWorld.createEntity {
            it += BlockComponent(block)
            it += BukkitBridgeComponent(MetadataKeys.BLOCK_ENTITY) { componentBridge -> Metadata.provide(componentBridge[BlockComponent].block) }
        }
    }

    fun removeBlockEntity(block: Block) {
        val entityToRemove = block.eEntity()?.entity ?: return
        removeEntity(entityToRemove)
    }
}

/**
 * 用于外部快捷地获取特定技能.
 */
object AbilityEntityQuery {
    fun createAbilityEntity(ability: Ability, input: AbilityInput): Entity {
        return WakameWorld.createEntity(ability.archetype.key) {
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
            if (entity[AbilityComponent].abilityId != abilityId)
                return@forEach
            if (entity[CastBy].caster != caster)
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
            if (entity[CastBy].caster != caster)
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
        val abilityComponent = get(AbilityComponent)
        return PlayerAbility(
            id = abilityComponent.abilityId,
            trigger = abilityComponent.trigger,
            variant = abilityComponent.variant,
            manaCost = abilityComponent.manaCost,
        )
    }
}