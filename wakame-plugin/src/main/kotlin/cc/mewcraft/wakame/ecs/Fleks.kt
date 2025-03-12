package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability.component.AbilityContainer
import cc.mewcraft.wakame.ability.system.AbilityInitSystem
import cc.mewcraft.wakame.ability.system.AbilityManaCostSystem
import cc.mewcraft.wakame.ability.system.AbilityRemoveSystem
import cc.mewcraft.wakame.ability.system.AbilityStatePhaseSystem
import cc.mewcraft.wakame.ability.system.AbilityTickResultSystem
import cc.mewcraft.wakame.ability.system.BlackholeSystem
import cc.mewcraft.wakame.ability.system.BlinkSystem
import cc.mewcraft.wakame.ability.system.DashSystem
import cc.mewcraft.wakame.ability.system.MultiJumpSystem
import cc.mewcraft.wakame.ecs.Fleks.world
import cc.mewcraft.wakame.ecs.system.BukkitBlockBridge
import cc.mewcraft.wakame.ecs.system.BukkitEntityBridge
import cc.mewcraft.wakame.ecs.system.ParticleSystem
import cc.mewcraft.wakame.ecs.system.TickCountSystem
import cc.mewcraft.wakame.element.component.ElementStackContainer
import cc.mewcraft.wakame.element.system.ElementStackSystem
import cc.mewcraft.wakame.item.logic.ItemSlotChangeEventInternals
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

@Init(stage = InitStage.POST_WORLD)
object Fleks : Listener {

    val world: World = configureWorld {

        families {

            Families.bootstrap()

            onAdd(Families.BUKKIT_PLAYER) { entity ->
                entity.configure {
                    it += AbilityContainer()
                    it += ElementStackContainer()
                }
            }

            onAdd(Families.BUKKIT_ENTITY) { entity ->
                entity.configure {
                    it += AbilityContainer()
                    it += ElementStackContainer()
                }
            }
        }

        systems {
            // BukkitObject 的删除系统优先于一切系统.

            add(BukkitEntityBridge())
            add(BukkitBlockBridge())

            // 给每个 BukkitPlayer 的 itemSlotChangeEvent 进行处理.
            add(ItemSlotChangeEventInternals())

            // 其它内部功能的移除系统

            add(AbilityRemoveSystem())
            add(AbilityTickResultSystem())
            add(ElementStackSystem())

            add(AbilityInitSystem())

            // 给每个 entity 的 tick 计数.

            add(TickCountSystem())

            // 根据标记与组件进行交互的系统, 例如技能.

            add(BlackholeSystem())
            add(BlinkSystem())
            add(DashSystem())
            add(MultiJumpSystem())

            // 消耗类系统, 可能会阻止进行下一阶段的系统.

            add(AbilityManaCostSystem())

            // 会改变状态的系统.

            add(AbilityStatePhaseSystem())

            add(ParticleSystem())
        }
    }

    /**
     * 创建一个 [cc.mewcraft.wakame.ecs.bridge.FleksEntity].
     */
    @PublishedApi
    internal inline fun createEntity(configuration: EntityCreateContext.(Entity) -> Unit = {}): Entity =
        world.entity {
            configuration.invoke(this, it)
        }

    /**
     * 修改一个 [cc.mewcraft.wakame.ecs.bridge.FleksEntity].
     */
    @PublishedApi
    internal inline fun editEntity(entity: Entity, configuration: EntityUpdateContext.(Entity) -> Unit) =
        with(world) {
            if (!contains(entity))
                error("Trying to edit entity ($entity) that does not exist in the world")
            entity.configure(configuration)
        }

    @InitFun
    fun init() {
        registerEvents() // 静态注册以提高性能
    }

    @DisableFun
    fun disable() {
        world.dispose()
    }

    /**
     * 更新一次 [world].
     */
    private fun tick() {
        try {
            // 我们是纯服务端开发, 因此 Fleks world 的更新频率应该和服务端 tick 的保持一致.
            // 这里将 world 的 deltaTime 始终设置为 1f 以忽略其“客户端方面”的作用(如动画).
            // 也就是说, 当创建一个 IntervalSystem 时, 其 interval 的参数的实际作用如下:
            // 1) EachFrame  =  每 tick 执行一次
            // 2) Fixed      =  每 N tick 执行一次
            //
            // 另外, deltaTime 的值没有任何作用. 程序猿只需要关注世界多久更新一次, 而不是每次更新花了多久时间.
            world.update(deltaTime = 1f)
        } catch (e: Exception) {
            LOGGER.error("在 ECS 更新时发生错误", e)
        }
    }

    @EventHandler
    private fun onServerTickStart(event: ServerTickStartEvent) {
        // 开发日记 24/12/14
        // 关于实现 Blink 技能时遇到的问题: 如果在这里使用 ServerTickEndEvent,
        // 那么在 tick 函数内调用 Player#setVelocity 后玩家的加速度会被服务端的内部逻辑覆盖, 导致玩家会在瞬移到空中后自由落体.
        // 解决方法: 在这里使用 ServerTickStartEvent, 让服务端内部的逻辑在 tick 函数返回之后执行.
        tick()
    }

}
