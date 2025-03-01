package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.ability.system.AbilityBukkitEntityMetadataSystem
import cc.mewcraft.wakame.ability.system.AbilityManaCostSystem
import cc.mewcraft.wakame.ability.system.AbilityMechanicRemoveSystem
import cc.mewcraft.wakame.ecs.WakameWorld.componentMap
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.MechanicComponent
import cc.mewcraft.wakame.ecs.component.Remove
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.component.BukkitBridgeComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.MechanicComponent
import cc.mewcraft.wakame.ecs.component.Remove
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.ecs.system.InitSystem
import cc.mewcraft.wakame.ecs.system.MechanicSystem
import cc.mewcraft.wakame.ecs.system.ParticleSystem
import cc.mewcraft.wakame.ecs.system.RemoveSystem
import cc.mewcraft.wakame.ecs.system.StatePhaseSystem
import cc.mewcraft.wakame.ecs.system.TickCountSystem
import cc.mewcraft.wakame.ecs.system.TickResultSystem
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import cc.mewcraft.wakame.ecs.system.InitSystem
import cc.mewcraft.wakame.ecs.system.MechanicSystem
import cc.mewcraft.wakame.ecs.system.ParticleSystem
import cc.mewcraft.wakame.ecs.system.RemoveSystem
import cc.mewcraft.wakame.ecs.system.StackCountSystem
import cc.mewcraft.wakame.ecs.system.StatePhaseSystem
import cc.mewcraft.wakame.ecs.system.TickCountSystem
import cc.mewcraft.wakame.ecs.system.TickResultSystem
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import me.lucko.helper.metadata.Metadata
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

object WakameWorld {

    private val instance: World = configureWorld {

        injectables {
            add(plugin)
            add(logger)
            add(this@WakameWorld)
        }

        families {
            onAdd(FamilyDefinitions.ABILITY) { entity ->
                val bukkitEntity = entity[BukkitBridgeComponent].bukkitEntity
                val metadataMap = Metadata.provide(bukkitEntity)
                metadataMap.put(MetadataKeys.ABILITY, ComponentMap(entity))
            }
            onRemove(FamilyDefinitions.ABILITY) { entity ->
                val bukkitEntity = entity[BukkitBridgeComponent].bukkitEntity
                Metadata.provide(bukkitEntity).remove(MetadataKeys.ABILITY)
            }
            onAdd(FamilyDefinitions.ELEMENT_STACK) { entity ->
                val bukkitEntity = entity[BukkitBridgeComponent].bukkitEntity
                val metadataMap = Metadata.provide(bukkitEntity)
                metadataMap.put(MetadataKeys.ELEMENT_STACK, ComponentMap(entity))
            }
            onRemove(FamilyDefinitions.ELEMENT_STACK) { entity ->
                val bukkitEntity = entity[BukkitBridgeComponent].bukkitEntity
                Metadata.provide(bukkitEntity).remove(MetadataKeys.ELEMENT_STACK)
            }
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

            add(StatePhaseSystem())
            add(StackCountSystem())
            add(AbilityMechanicRemoveSystem())
            add(TickResultSystem())

            add(ParticleSystem())

            // 将所有标记重置到默认状态, 应当放在末尾.

            add(InitSystem())
        }
    }

    private val mechanicFamily: Family = instance.family { all(IdentifierComponent, MechanicComponent) }

    internal fun tick() {
        with(instance) {
            update(1f)
        }
    }

    internal fun world(): World {
        return instance
    }

    internal inline fun createEntity(identifier: String, configuration: EntityCreateContext.(Entity) -> Unit = {}) {
        instance.entity {
            it += IdentifierComponent(identifier)
            configuration.invoke(this, it)
        }
    }

    internal fun addMechanic(identifier: String, mechanic: Mechanic, replace: Boolean = true) {
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

    internal fun removeMechanic(identifier: String) {
        with(instance) {
            val entityToRemove = mechanicFamily
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