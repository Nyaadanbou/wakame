package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.Remove
import cc.mewcraft.wakame.ecs.system.ComponentMapSystem
import cc.mewcraft.wakame.skill2.system.MechanicCooldownSystem
import cc.mewcraft.wakame.ecs.system.RemoveSystem
import cc.mewcraft.wakame.ecs.system.ResultSystem
import cc.mewcraft.wakame.ecs.system.TickCountSystem
import cc.mewcraft.wakame.ecs.system.TickResultSystem
import cc.mewcraft.wakame.skill2.system.MechanicBukkitEntityMetadataSystem
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.configureWorld

class WakameWorld(
    private val plugin: WakamePlugin,
) {
    val instance: World = configureWorld {

        injectables {
            add(plugin)
            add(this@WakameWorld)
        }

        systems {
            // 关于顺序: 删除系统优先于一切系统, 随后是将当前 entity 信息存入外部结构.
            add(RemoveSystem())
            add(ComponentMapSystem())
            add(TickCountSystem())
            add(MechanicBukkitEntityMetadataSystem())
            add(MechanicCooldownSystem())
            add(ResultSystem())
            add(TickResultSystem())
        }
    }


    fun tick() {
        instance.update(1f)
    }

    private val identifierFamily: Family
        get() = family { all(IdentifierComponent) }

    fun createEntity(identifier: String, configuration: EntityCreateContext.(Entity) -> Unit = {}) {
        instance.entity {
            it += IdentifierComponent(identifier)
            configuration.invoke(this, it)
        }
    }

    fun editEntity(identifier: String, configuration: EntityUpdateContext.(Entity) -> Unit) {
        with(instance) {
            val entityToModify = identifierFamily.firstOrNull { it[IdentifierComponent].id == identifier } ?: return
            editEntity(entityToModify, configuration)
        }
    }

    fun editEntity(entity: Entity, configuration: EntityUpdateContext.(Entity) -> Unit) {
        with(instance) {
            entity.configure(configuration)
        }
    }

    fun editEntities(family: Family, configuration: EntityUpdateContext.(Entity) -> Unit) {
        with(instance) {
            family.forEach {
                it.configure(configuration)
            }
        }
    }

    fun removeEntity(identifier: String) {
        with(instance) {
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
}