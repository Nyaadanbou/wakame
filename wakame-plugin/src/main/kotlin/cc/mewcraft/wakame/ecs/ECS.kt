package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ability.system.AbilityManaCostSystem
import cc.mewcraft.wakame.ability.system.AbilityRemoveSystem
import cc.mewcraft.wakame.ability.system.AbilityStatePhaseSystem
import cc.mewcraft.wakame.ability.system.BlackHoleSystem
import cc.mewcraft.wakame.ability.system.BlinkSystem
import cc.mewcraft.wakame.ability.system.DashSystem
import cc.mewcraft.wakame.ability.system.ExtraJumpSystem
import cc.mewcraft.wakame.ecs.component.BukkitBridgeComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.MechanicComponent
import cc.mewcraft.wakame.ecs.component.Remove
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.external.BlockEntityQuery
import cc.mewcraft.wakame.ecs.external.BukkitEntityEntityQuery
import cc.mewcraft.wakame.ecs.external.KoishEntity
import cc.mewcraft.wakame.ecs.external.PlayerEntityQuery
import cc.mewcraft.wakame.ecs.system.BlockRemoveSystem
import cc.mewcraft.wakame.ecs.system.InitSystem
import cc.mewcraft.wakame.ecs.system.MechanicSystem
import cc.mewcraft.wakame.ecs.system.ParticleSystem
import cc.mewcraft.wakame.ecs.system.RemoveSystem
import cc.mewcraft.wakame.ecs.system.StackCountSystem
import cc.mewcraft.wakame.ecs.system.TickCountSystem
import cc.mewcraft.wakame.ecs.system.TickResultSystem
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.event
import cc.mewcraft.wakame.util.metadata.Metadata
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.FamilyConfiguration
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkUnloadEvent
import kotlin.jvm.optionals.getOrNull
import org.bukkit.entity.Entity as BukkitEntity

@Init(stage = InitStage.POST_WORLD)
object ECS {

    private val instance: World = configureWorld {

        families {
            recordToBukkitMetadata(family = FamilyDefinitions.PLAYER)
            recordToBukkitMetadata(family = FamilyDefinitions.BLOCK)
            recordToBukkitMetadata(family = FamilyDefinitions.BUKKIT_ENTITY)
        }

        systems {
            // 关于顺序: 删除系统优先于一切系统.
            add(RemoveSystem())

            // 给每个 entity 的 tick 计数.
            add(TickCountSystem())

            // 根据标记与组件进行交互的系统, 例如技能.

            add(MechanicSystem())

            add(BlackHoleSystem())
            add(BlinkSystem())
            add(DashSystem())
            add(ExtraJumpSystem())

            // 消耗类系统, 可能会阻止进行下一阶段的系统.

            add(AbilityManaCostSystem())

            // 会改变状态的系统.

            add(AbilityStatePhaseSystem())
            add(StackCountSystem())
            add(AbilityRemoveSystem())
            add(BlockRemoveSystem())
            add(TickResultSystem())

            add(ParticleSystem())

            // 将所有标记重置到默认状态, 应当放在末尾.

            add(InitSystem())
        }
    }

    private fun FamilyConfiguration.recordToBukkitMetadata(family: Family) {
        onAdd(family) { entity ->
            val metadataKey = entity[BukkitBridgeComponent].metadataKey
            val metadataMap = entity[BukkitBridgeComponent].metadataMapProvider(KoishEntity(entity))
            metadataMap.put(metadataKey, KoishEntity(entity))
        }

        onRemove(family) { entity ->
            val metadataKey = entity[BukkitBridgeComponent].metadataKey
            val metadataMap = entity[BukkitBridgeComponent].metadataMapProvider(KoishEntity(entity))
            metadataMap.remove(metadataKey)
        }
    }

    @InitFun
    private fun init() {
        event<PlayerJoinEvent> { event ->
            val player = event.player
            PlayerEntityQuery.createPlayerEntity(player)
        }

        event<PlayerQuitEvent> { event ->
            val player = event.player
            PlayerEntityQuery.removePlayerEntity(player)
        }

        event<ChunkUnloadEvent> { event ->
            val chunk = event.chunk
            val entities = chunk.entities
            for (entity in entities) {
                BukkitEntityEntityQuery.removeBukkitEntityEntity(entity)
            }
        }
    }

    @DisableFun
    private fun disable() {
        instance.dispose()
    }

    internal fun tick() {
        instance.update(1f)
    }

    internal fun world(): World {
        return instance
    }

    internal inline fun createEntity(configuration: EntityCreateContext.(Entity) -> Unit = {}): Entity {
        return instance.entity {
            configuration.invoke(this, it)
        }
    }

    internal inline fun createEntity(identifier: Identifier, configuration: EntityCreateContext.(Entity) -> Unit = {}): Entity {
        return instance.entity {
            it += IdentifierComponent(identifier)
            configuration.invoke(this, it)
        }
    }

    internal fun addMechanic(identifier: Identifier, mechanic: Mechanic, replace: Boolean = true) {
        with(instance) {
            val entityToModify = FamilyDefinitions.MECHANIC.firstOrNull { it[IdentifierComponent].id == identifier }
            if (entityToModify != null) {
                if (replace) {
                    entityToModify[MechanicComponent].mechanic = mechanic
                }
                return
            }
            createEntity(identifier) {
                it += Tags.DISPOSABLE
                it += MechanicComponent(mechanic)
                it += TickCountComponent()
            }
        }
    }

    internal fun removeMechanic(identifier: Identifier) {
        with(instance) {
            val entityToRemove = FamilyDefinitions.MECHANIC
                .firstOrNull { it[IdentifierComponent].id == identifier }
            if (entityToRemove == null) {
                error("Tried to remove mechanic that does not exist: $identifier")
            }
            removeEntity(entityToRemove)
        }
    }

    internal inline fun editEntity(entity: Entity, configuration: EntityUpdateContext.(Entity) -> Unit) {
        with(instance) {
            if (!contains(entity)) {
                error("Tried to edit entity that does not exist: $entity")
            }
            entity.configure(configuration)
        }
    }

    internal fun editEntities(family: Family, configuration: EntityUpdateContext.(Entity) -> Unit) {
        with(instance) {
            family.forEach {
                it.configure(configuration)
            }
        }
    }

    internal fun removeEntity(entity: Entity) {
        with(instance) {
            if (!contains(entity)) {
                error("Tried to remove entity that does not exist: $entity")
            }
            entity.configure { it += Remove }
        }
    }
}

fun Player.eEntity(): KoishEntity {
    val metadataMap = Metadata.provide(this)
    return metadataMap[MetadataKeys.PLAYER_ENTITY].get()
}

fun BukkitEntity.eEntityOrCreate(): KoishEntity {
    if (this is Player) {
        return this.eEntity()
    }
    val metadataMap = Metadata.provide(this)
    if (!metadataMap.has(MetadataKeys.BUKKIT_ENTITY_ENTITY)) {
        BukkitEntityEntityQuery.createBukkitEntity(this)
    }
    return metadataMap[MetadataKeys.BUKKIT_ENTITY_ENTITY].get()
}

fun BukkitEntity.eEntity(): KoishEntity? {
    if (this is Player) {
        return this.eEntity()
    }
    val metadataMap = Metadata.provide(this)
    return metadataMap[MetadataKeys.BUKKIT_ENTITY_ENTITY].getOrNull()
}

fun Block.eEntityOrCreate(): KoishEntity {
    val metadataMap = Metadata.provide(this)
    if (!metadataMap.has(MetadataKeys.BLOCK_ENTITY)) {
        BlockEntityQuery.createBlockEntity(this)
    }
    return metadataMap[MetadataKeys.BLOCK_ENTITY].get()
}

fun Block.eEntity(): KoishEntity? {
    val metadataMap = Metadata.provide(this)
    return metadataMap[MetadataKeys.BLOCK_ENTITY].getOrNull()
}