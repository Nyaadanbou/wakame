package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.ecs.bridge.EEntity
import cc.mewcraft.wakame.ecs.system.CountTick
import cc.mewcraft.wakame.ecs.system.DisplayMana
import cc.mewcraft.wakame.ecs.system.InitMana
import cc.mewcraft.wakame.ecs.system.ManageBossBar
import cc.mewcraft.wakame.ecs.system.RemoveBukkitBlocks
import cc.mewcraft.wakame.ecs.system.RemoveBukkitEntities
import cc.mewcraft.wakame.ecs.system.RenderParticle
import cc.mewcraft.wakame.ecs.system.RestoreMana
import cc.mewcraft.wakame.ecs.system.UpdateEntityInfoBossBar
import cc.mewcraft.wakame.ecs.system.UpdateMaxMana
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InternalInit
import cc.mewcraft.wakame.lifecycle.initializer.InternalInitStage
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.registerEvents
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityCreateContext
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

private val GLOBAL_FLEKS_CONFIG = Configs["fleks"]

@InternalInit(stage = InternalInitStage.POST_WORLD)
internal object KoishFleks : Listener, Fleks, FleksAdder {
    private val systemOrder: List<Identifier> by GLOBAL_FLEKS_CONFIG.entry("system_order")

    override val world: World = configureWorld {

        families {
            BuiltInRegistries.FAMILIES_BOOTSTRAPPER.add("common_families") { CommonFamilies }

            BuiltInRegistries.FAMILIES_BOOTSTRAPPER.freeze()
            BuiltInRegistries.FAMILIES_BOOTSTRAPPER.forEach { it.bootstrap() }
        }

        systems {
            addToRegistrySystem("count_tick") { CountTick } // 记录 entity 存在的 tick 数
            addToRegistrySystem("display_mana") { DisplayMana } // 显示玩家的魔法值
            addToRegistrySystem("init_mana") { InitMana } // 初始化玩家的魔法值
            addToRegistrySystem("manage_boss_bar") { ManageBossBar } // 显示/移除 boss bar
            addToRegistrySystem("remove_bukkit_entities") { RemoveBukkitEntities } // 移除无效 bukkit entity 所映射的 ecs entity
            addToRegistrySystem("remove_bukkit_blocks") { RemoveBukkitBlocks } // 移除无效 bukkit block 所映射的 ecs entity
            addToRegistrySystem("render_particle") { RenderParticle } // 渲染粒子效果
            addToRegistrySystem("restore_mana") { RestoreMana } // 恢复玩家的魔法值
            addToRegistrySystem("update_entity_info_boss_bar") { UpdateEntityInfoBossBar } // 更新各种关于 boss bar 的逻辑
            addToRegistrySystem("update_max_mana") { UpdateMaxMana } // 更新玩家的最大魔法值

            BuiltInRegistries.SYSTEM_BOOTSTRAPPER.freeze()

            BuiltInRegistries.SYSTEM_BOOTSTRAPPER
                .keys
                .filter { it.value !in systemOrder }
                .also { LOGGER.info("未启用的 ECS 系统: $it") }
            for (order in systemOrder) {
                val system = BuiltInRegistries.SYSTEM_BOOTSTRAPPER[order]
                    ?: error("无法找到系统 $order, 请检查配置文件")
                add(system.bootstrap())
            }
        }
    }

    /**
     * 创建一个 [EEntity].
     */
    override fun createEntity(configuration: EntityCreateContext.(Entity) -> Unit): EEntity = world.entity {
        configuration.invoke(this, it)
    }

    /**
     * 修改一个 [EEntity].
     */
    override fun editEntity(entity: Entity, configuration: EntityUpdateContext.(Entity) -> Unit) = with(world) {
        if (!contains(entity)) error("Trying to edit entity ($entity) that does not exist in the world")
        entity.configure(configuration)
    }

    @InitFun
    fun init() {
        Fleks.register(this)
        registerEvents() // 静态注册以提高性能
    }

    @DisableFun
    fun disable() {
        world.dispose()
    }

    @EventHandler
    fun on(event: ServerTickStartEvent) {
        // 开发日记 24/12/14
        // 关于实现 Blink 技能时遇到的问题: 如果在这里使用 ServerTickEndEvent,
        // 那么在 tick 函数内调用 Player#setVelocity 后玩家的加速度会被服务端的内部逻辑覆盖, 导致玩家会在瞬移到空中后自由落体.
        // 解决方法: 在这里使用 ServerTickStartEvent, 让服务端内部的逻辑在 tick 函数返回之后执行.

        //
        // 更新一次世界
        //

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

}
