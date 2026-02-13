package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.bridge.EEntity
import cc.mewcraft.wakame.ecs.system.RemoveBukkitBlocks
import cc.mewcraft.wakame.ecs.system.RemoveBukkitEntities
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InternalInit
import cc.mewcraft.wakame.lifecycle.initializer.InternalInitStage
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.KoishKeys
import cc.mewcraft.wakame.util.registerEvents
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import com.github.quillraven.fleks.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

@InternalInit(InternalInitStage.POST_WORLD)
internal object KoishFleks : Listener, Fleks, FleksPatcher {

    private val SYSTEM_ORDER: List<KoishKey> = listOf(
        "init_item_cooldown_container",
        "init_attribute_container",
        "init_kizami_container",
        "scan_item_slot_changes",
        "play_attack_speed_animation",
        "apply_attribute_effects",
        "apply_kizami_effects",
        "switch_katana",
        "tick_katana",
        "apply_enchantment_effect",
        "tick_antigrav_shot_enchantment",
        "tick_attribute_enchantment",
        "tick_blast_mining_enchantment",
        "tick_fragile_enchantment",
        "tick_smelter_enchantment",
        "tick_veinminer_enchantment",
        "tick_void_escape_enchantment"
    ).map(KoishKeys::of)

    override val world: World = configureWorld {

        families {
            BuiltInRegistries.FAMILIES_BOOTSTRAPPER.add("common_families") { CommonFamilies }

            BuiltInRegistries.FAMILIES_BOOTSTRAPPER.freeze()
            BuiltInRegistries.FAMILIES_BOOTSTRAPPER.forEach { it.bootstrap() }
        }

        systems {
            BuiltInRegistries.SYSTEM_BOOTSTRAPPER.freeze()

            add(RemoveBukkitEntities)
            add(RemoveBukkitBlocks)

            val unloadSystems = BuiltInRegistries.SYSTEM_BOOTSTRAPPER.keys.filter { it.value !in SYSTEM_ORDER }.map { it.value.value() }
            if (unloadSystems.isNotEmpty()) {
                LOGGER.info("未启用的 ECS system: ${unloadSystems.joinToString(", ")}")
            }
            for (order in SYSTEM_ORDER) {
                val systemBootstrapper = BuiltInRegistries.SYSTEM_BOOTSTRAPPER[order] ?: continue
                val system = systemBootstrapper.bootstrap() ?: continue
                add(system)
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
