package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ability.system.AbilityManaCostSystem
import cc.mewcraft.wakame.ability.system.AbilityMechanicRemoveSystem
import cc.mewcraft.wakame.ability.system.AbilityStatePhaseSystem
import cc.mewcraft.wakame.ecs.component.BukkitBridgeComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.MechanicComponent
import cc.mewcraft.wakame.ecs.component.Remove
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.external.ComponentBridge
import cc.mewcraft.wakame.ecs.system.InitSystem
import cc.mewcraft.wakame.ecs.system.MechanicSystem
import cc.mewcraft.wakame.ecs.system.ParticleSystem
import cc.mewcraft.wakame.ecs.system.RemoveSystem
import cc.mewcraft.wakame.ecs.system.StackCountSystem
import cc.mewcraft.wakame.ecs.system.TickCountSystem
import cc.mewcraft.wakame.ecs.system.TickResultSystem
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.metadata.Metadata
import cc.mewcraft.wakame.util.metadata.MetadataKey
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.FamilyConfiguration
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld

object WakameWorld {

    private val instance: World = configureWorld {

        families {
            recordToBukkitEntity(FamilyDefinitions.ABILITY_BUKKIT_BRIDGE)
            recordToBukkitEntity(FamilyDefinitions.ELEMENT_STACK_BUKKIT_BRIDGE)
        }

        systems {
            // 关于顺序: 删除系统优先于一切系统.
            add(RemoveSystem())

            // 根据标记与组件进行交互的系统

            add(TickCountSystem())
            add(MechanicSystem())

            // 消耗类系统, 可能会阻止进行下一阶段的系统

            add(AbilityManaCostSystem())

            // 会改变状态的系统

            add(AbilityStatePhaseSystem())
            add(StackCountSystem())
            add(AbilityMechanicRemoveSystem())
            add(TickResultSystem())

            add(ParticleSystem())

            // 将所有标记重置到默认状态, 应当放在末尾.

            add(InitSystem())
        }
    }

    private fun FamilyConfiguration.recordToBukkitEntity(family: Family) {
        onAdd(family) { entity ->
            val identifier = entity[IdentifierComponent].id
            val bukkitEntity = entity[BukkitBridgeComponent].bukkitEntity
            val metadataMap = Metadata.provide(bukkitEntity)
            val metadataKey = MetadataKey.create(identifier.asString(), ComponentBridge::class.java)
            metadataMap.put(metadataKey, ComponentBridge(entity))
        }

        onRemove(family) { entity ->
            val identifier = entity[IdentifierComponent].id
            val bukkitEntity = entity[BukkitBridgeComponent].bukkitEntity
            val metadataMap = Metadata.provide(bukkitEntity)
            val metadataKey = MetadataKey.create(identifier.asString(), ComponentBridge::class.java)
            metadataMap.remove(metadataKey)
        }
    }

    internal fun tick() {
        instance.update(1f)
    }

    internal fun world(): World {
        return instance
    }

    internal inline fun createEntity(identifier: Identifier, configuration: EntityCreateContext.(Entity) -> Unit = {}) {
        instance.entity {
            it += IdentifierComponent(identifier)
            configuration.invoke(this, it)
        }
    }

    internal fun addMechanic(identifier: Identifier, mechanic: Mechanic, replace: Boolean = true) {
        with(instance) {
            val identifierFamily = family { all(IdentifierComponent, MechanicComponent) }
            val entityToModify = identifierFamily.firstOrNull { it[IdentifierComponent].id == identifier }
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