package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability2.system.AbilityActivator
import cc.mewcraft.wakame.ability2.system.AbilityRemover
import cc.mewcraft.wakame.ability2.system.AbilityStateInitializer
import cc.mewcraft.wakame.ability2.system.AbilityStateManager
import cc.mewcraft.wakame.ability2.system.AbilityTickUpdater
import cc.mewcraft.wakame.ability2.system.BlackholeAbility
import cc.mewcraft.wakame.ability2.system.BlinkAbility
import cc.mewcraft.wakame.ability2.system.DashAbility
import cc.mewcraft.wakame.ability2.system.InitAbilityContainer
import cc.mewcraft.wakame.ability2.system.InitPlayerCombo
import cc.mewcraft.wakame.ability2.system.ManaCostHandler
import cc.mewcraft.wakame.ability2.system.MultiJumpAbility
import cc.mewcraft.wakame.ecs.bridge.EEntity
import cc.mewcraft.wakame.ecs.system.BossBarVisibility
import cc.mewcraft.wakame.ecs.system.BukkitBlockBridge
import cc.mewcraft.wakame.ecs.system.BukkitEntityBridge
import cc.mewcraft.wakame.ecs.system.EntityInfoBossBarManager
import cc.mewcraft.wakame.ecs.system.ManaHandler
import cc.mewcraft.wakame.ecs.system.ManaHud
import cc.mewcraft.wakame.ecs.system.ParticleHandler
import cc.mewcraft.wakame.ecs.system.TickCounter
import cc.mewcraft.wakame.element.system.ElementStack
import cc.mewcraft.wakame.element.system.InitElementStackContainer
import cc.mewcraft.wakame.enchantment2.system.AntigravShotEnchantmentHandler
import cc.mewcraft.wakame.enchantment2.system.AttributeEnchantmentHandler
import cc.mewcraft.wakame.enchantment2.system.BlastMiningEnchantmentHandler
import cc.mewcraft.wakame.enchantment2.system.EnchantmentEffectApplier
import cc.mewcraft.wakame.enchantment2.system.FragileEnchantmentHandler
import cc.mewcraft.wakame.enchantment2.system.SmelterEnchantmentHandler
import cc.mewcraft.wakame.enchantment2.system.VeinminerEnchantmentHandler
import cc.mewcraft.wakame.entity.attribute.system.InitAttributeContainer
import cc.mewcraft.wakame.entity.player.system.InitAttackSpeedContainer
import cc.mewcraft.wakame.item2.ItemSlotChangeMonitor2
import cc.mewcraft.wakame.item2.behavior.system.AttributeEffectApply
import cc.mewcraft.wakame.item2.behavior.system.KizamiEffectApply
import cc.mewcraft.wakame.kizami2.system.InitKizamiContainer
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
internal object KoishFleks : Listener, Fleks {

    override val world: World = configureWorld {

        families {
            Families.bootstrap()
        }

        systems {
            add(BukkitEntityBridge) // 移除无效 bukkit entity 所映射的 ecs entity
            add(BukkitBlockBridge) // 移除无效 bukkit block 所映射的 ecs entity

            // ------------
            // 属性
            // ------------

            add(InitAbilityContainer)
            add(InitAttackSpeedContainer)
            add(InitAttributeContainer)
            add(InitElementStackContainer)
            add(InitKizamiContainer)
            add(InitPlayerCombo)

            // ------------
            // 物品
            // ------------

            add(ItemSlotChangeMonitor2) // 监听背包物品变化
            add(AttributeEffectApply)
            add(KizamiEffectApply)

            // -------------
            // 带“移除”的系统 ???
            // -------------

            add(AbilityActivator) // “激活”玩家装备的技能
            add(AbilityRemover) // “移除”玩家装备的技能
            add(AbilityTickUpdater) // 根据 TickResult 更新 entity
            add(ElementStack) // 元素特效层数
            add(AbilityStateInitializer) //  tick 初始化技能的状态
            add(EntityInfoBossBarManager) // 各种关于 boss bar 的逻辑
            add(BossBarVisibility) // 显示/移除 boss bar
            add(TickCounter) // 记录 entity 存在的 tick 数

            // ------------
            // 技能
            // ------------

            add(BlackholeAbility)
            add(BlinkAbility)
            add(DashAbility)
            add(MultiJumpAbility)
            add(ManaCostHandler) // 消耗使用技能的魔法值
            add(AbilityStateManager) // 管理技能的当前状态

            // ------------
            // 附魔
            // ------------

            add(EnchantmentEffectApplier) // framework
            add(AntigravShotEnchantmentHandler)
            add(AttributeEnchantmentHandler)
            add(BlastMiningEnchantmentHandler)
            add(FragileEnchantmentHandler)
            add(SmelterEnchantmentHandler)
            add(VeinminerEnchantmentHandler)

            // ------------
            // 资源
            // ------------

            add(ManaHandler)
            add(ManaHud)

            // ------------
            // 粒子
            // -------------

            add(ParticleHandler)
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
