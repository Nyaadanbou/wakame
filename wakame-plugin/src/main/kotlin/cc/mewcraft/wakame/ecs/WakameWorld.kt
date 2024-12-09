package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.Remove
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.ecs.system.*
import cc.mewcraft.wakame.skill2.system.MechanicBukkitEntityMetadataSystem
import cc.mewcraft.wakame.skill2.system.MechanicConditionSessionSystem
import cc.mewcraft.wakame.skill2.system.MechanicConditionSystem
import cc.mewcraft.wakame.skill2.system.MechanicCooldownSystem
import com.github.quillraven.fleks.*
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

class WakameWorld(
    private val plugin: WakamePlugin,
) {
    companion object : KoinComponent {
        private val logger: Logger by inject()
    }

    /**
     * 缓存了单个 tick 内的 [ComponentMap], 每次更新会进行清除.
     *
     * 目的是为了在一个 tick 多次调用 [componentMap] 能速度变快.
     */
    private val componentMapCache: Object2ObjectOpenHashMap<Entity, ComponentMap> = Object2ObjectOpenHashMap()

    val instance: World = configureWorld {

        injectables {
            add(plugin)
            add(logger)
            add(this@WakameWorld)
        }

        systems {
            // 关于顺序: 删除系统优先于一切系统.
            add(RemoveSystem())

            // 修改标记的系统

            add(MechanicCooldownSystem())
            add(MechanicConditionSystem())
            add(MechanicConditionSessionSystem())

            // 同步 ComponentMap 给外部

            add(MechanicBukkitEntityMetadataSystem())

            // 根据标记与组件进行交互的系统

            add(TickCountSystem())
            add(ResultSystem())
            add(TickResultSystem())
            add(StatePhaseSystem())

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

    fun createEntity(identifier: String, configuration: EntityCreateContext.(Entity) -> Unit = {}) {
        instance.entity {
            it += IdentifierComponent(identifier)
            configuration.invoke(this, it)
        }
    }

    fun editEntity(identifier: String, configuration: EntityUpdateContext.(Entity) -> Unit) {
        with(instance) {
            val identifierFamily = family { all(IdentifierComponent) }
            val entityToModify = identifierFamily.firstOrNull { it[IdentifierComponent].id == identifier } ?: return
            editEntity(entityToModify, configuration)
        }
    }

    fun editEntity(entity: Entity, configuration: EntityUpdateContext.(Entity) -> Unit) {
        with(instance) {
            entity.configure(configuration)
        }
    }

    fun editEntities(family: World.() -> Family, configuration: EntityUpdateContext.(Entity) -> Unit) {
        with(instance) {
            val family = family()
            family.forEach {
                it.configure(configuration)
            }
        }
    }

    fun removeEntity(identifier: String) {
        with(instance) {
            val identifierFamily = family { all(IdentifierComponent) }
            val entityToRemove = identifierFamily
                .firstOrNull { it[IdentifierComponent].id == identifier } ?: return
            entityToRemove.configure {
                it += Remove
            }
        }
    }

    fun removeEntity(entity: Entity) {
        with(instance) {
            entity.configure { it += Remove }
        }
    }

    fun componentMap(entity: Entity): ComponentMap {
        return this.componentMapCache.computeIfAbsent(entity, Object2ObjectFunction { ComponentMap(instance, entity) })
    }
}