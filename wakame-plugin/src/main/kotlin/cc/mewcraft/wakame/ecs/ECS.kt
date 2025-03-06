package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability.system.AbilityManaCostSystem
import cc.mewcraft.wakame.ability.system.AbilityRemoveSystem
import cc.mewcraft.wakame.ability.system.AbilityStatePhaseSystem
import cc.mewcraft.wakame.ability.system.BlackHoleSystem
import cc.mewcraft.wakame.ability.system.BlinkSystem
import cc.mewcraft.wakame.ability.system.DashSystem
import cc.mewcraft.wakame.ability.system.ExtraJumpSystem
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.Remove
import cc.mewcraft.wakame.ecs.system.BlockRemoveSystem
import cc.mewcraft.wakame.ecs.system.BukkitEntityRemoveSystem
import cc.mewcraft.wakame.ecs.system.InitSystem
import cc.mewcraft.wakame.ecs.system.ParticleSystem
import cc.mewcraft.wakame.ecs.system.RemoveSystem
import cc.mewcraft.wakame.ecs.system.StackCountSystem
import cc.mewcraft.wakame.ecs.system.TickCountSystem
import cc.mewcraft.wakame.ecs.system.TickResultSystem
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.util.Identifier
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld

object ECS {

    private val world: World = configureWorld {

        families {
            // 初始化 FamilyDefinitions
            FamilyDefinitions
        }

        systems {
            // 关于顺序: 删除系统优先于一切系统.
            add(RemoveSystem())

            // 给每个 entity 的 tick 计数.
            add(TickCountSystem())

            // 根据标记与组件进行交互的系统, 例如技能.

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
            add(BukkitEntityRemoveSystem())
            add(TickResultSystem())

            add(ParticleSystem())

            // 将所有标记重置到默认状态, 应当放在末尾.

            add(InitSystem())
        }
    }

    @DisableFun
    private fun disable() {
        world.dispose()
    }

    internal fun tick() {
        try {
            world.update(1f)
        } catch (e: Exception) {
            LOGGER.error("在 ECS 更新时发生错误", e)
        }
    }

    internal fun world(): World {
        return world
    }

    internal inline fun createEntity(identifier: Identifier? = null, configuration: EntityCreateContext.(Entity) -> Unit = {}): Entity {
        return world.entity {
            identifier?.let { id -> it += IdentifierComponent(id) }
            configuration.invoke(this, it)
        }
    }

    internal inline fun editEntity(entity: Entity, configuration: EntityUpdateContext.(Entity) -> Unit) {
        with(world) {
            if (!contains(entity)) {
                error("Tried to edit entity that does not exist: $entity")
            }
            entity.configure(configuration)
        }
    }

    internal fun editEntities(family: Family, configuration: EntityUpdateContext.(Entity) -> Unit) {
        with(world) {
            family.forEach {
                it.configure(configuration)
            }
        }
    }

    internal fun removeEntity(entity: Entity) {
        with(world) {
            if (!contains(entity)) {
                error("Tried to remove entity that does not exist: $entity")
            }
            entity.configure { it += Remove }
        }
    }
}



