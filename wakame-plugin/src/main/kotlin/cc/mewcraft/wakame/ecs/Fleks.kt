package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability.component.AbilityContainer
import cc.mewcraft.wakame.ability.system.*
import cc.mewcraft.wakame.ecs.Fleks.world
import cc.mewcraft.wakame.ecs.system.*
import cc.mewcraft.wakame.element.component.ElementStackContainer
import cc.mewcraft.wakame.element.system.ElementStackSystem
import cc.mewcraft.wakame.enchantment2.system.*
import cc.mewcraft.wakame.item.logic.ItemSlotChangeMonitor
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import com.github.quillraven.fleks.*
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
            add(BukkitEntityBridge()) // 移除无效 bukkit entity 所映射的 ecs entity
            add(BukkitBlockBridge()) // 移除无效 bukkit block 所映射的 ecs entity
            add(ItemSlotChangeMonitor()) // 监听背包物品变化

            // -------------
            // 带“移除”的系统 ???
            // -------------

            add(AbilityAddSystem()) // “激活”玩家装备的技能
            add(AbilityRemoveSystem()) // “移除”玩家装备的技能
            add(AbilityTickResultSystem()) // 根据 TickResult 更新 entity
            add(ElementStackSystem()) // 元素特效
            add(AbilityInitSystem()) // ???
            add(EntityInfoBossBar()) // 各种关于 boss bar 的逻辑
            add(BossBarVisibleManager()) // 显示/移除 boss bar
            add(TickCountSystem()) // 记录 entity 存在的 tick 数

            // ------------
            // 技能
            // ------------

            add(BlackholeSystem())
            add(BlinkSystem())
            add(DashSystem())
            add(MultiJumpSystem())
            add(AbilityManaCostSystem()) // 消耗使用技能的魔法值
            add(AbilityStatePhaseSystem()) // 管理技能的当前状态

            // ------------
            // 附魔
            // ------------

            add(EnchantmentEffectApplier) // framework
            add(EnchantmentAttributeSystem)
            add(EnchantmentAntigravShotSystem)
            add(EnchantmentSmelterSystem)
            add(EnchantmentBlastMiningSystem)
            add(EnchantmentFragileSystem)

            // ------------
            // 粒子
            // -------------

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
