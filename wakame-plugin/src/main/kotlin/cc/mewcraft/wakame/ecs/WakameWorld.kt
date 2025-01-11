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
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap

object WakameWorld {

    /**
     * 缓存了单个 tick 内的 [ComponentMap], 每次更新会进行清除.
     *
     * 目的是为了在一个 tick 多次调用 [componentMap] 能速度变快.
     */
    private val componentMapCache: Object2ObjectOpenHashMap<Entity, ComponentMap> = Object2ObjectOpenHashMap()

    private val instance: World = configureWorld {

        systems {
            // 关于顺序: 删除系统优先于一切系统.
            add(RemoveSystem())

            // 同步 ComponentMap 给外部

            add(AbilityBukkitEntityMetadataSystem())

            // 根据标记与组件进行交互的系统

            add(TickCountSystem())
            add(MechanicSystem())

            // 消耗类系统, 可能会阻止进行下一阶段的系统

            add(AbilityManaCostSystem())

            // 会改变状态的系统

            add(StatePhaseSystem())
            add(AbilityMechanicRemoveSystem())
            add(TickResultSystem())

            add(ParticleSystem())

            // 将所有标记重置到默认状态, 应当放在末尾.

            add(InitSystem())
        }
    }


    internal fun tick() {
        with(instance) {
            componentMapCache.clear()
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

    internal fun createMechanic(identifier: String, replace: Boolean = true, mechanicProvider: () -> Mechanic) {
        with(instance) {
            val identifierFamily = family { all(IdentifierComponent, MechanicComponent) }
            val entityToModify = identifierFamily.firstOrNull { it[IdentifierComponent].id == identifier }
            if (entityToModify != null) {
                if (replace) {
                    entityToModify[MechanicComponent].mechanic = mechanicProvider()
                }
                return
            }
            createEntity(identifier) {
                it += EntityType.MECHANIC
                it += Tags.DISPOSABLE
                it += MechanicComponent(mechanicProvider())
                it += TickCountComponent()
            }
        }
    }

    internal fun removeMechanic(identifier: String) {
        with(instance) {
            val identifierFamily = family { all(IdentifierComponent, EntityType.MECHANIC) }
            val entityToRemove = identifierFamily
                .firstOrNull { it[IdentifierComponent].id == identifier }
            if (entityToRemove == null) {
                error("Tried to remove mechanic that does not exist: $identifier")
            }
            entityToRemove.configure {
                it += Remove
            }
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

    internal inline fun editEntities(family: World.() -> Family, noinline configuration: EntityUpdateContext.(Entity) -> Unit) {
        with(instance) {
            val family = family()

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

    fun componentMap(entity: Entity): ComponentMap {
        return this.componentMapCache.computeIfAbsent(entity, Object2ObjectFunction { ComponentMap(entity) })
    }
}