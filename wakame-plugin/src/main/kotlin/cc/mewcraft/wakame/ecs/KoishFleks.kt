package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability2.system.AbilityActivator
import cc.mewcraft.wakame.ability2.system.AbilityRemover
import cc.mewcraft.wakame.ability2.system.ConsumeManaForAbilities
import cc.mewcraft.wakame.ability2.system.InitAbilityContainer
import cc.mewcraft.wakame.ability2.system.InitPlayerCombo
import cc.mewcraft.wakame.ability2.system.RenderOnceOffItemName
import cc.mewcraft.wakame.ability2.system.TickAbilityBlackhole
import cc.mewcraft.wakame.ability2.system.TickAbilityBlink
import cc.mewcraft.wakame.ability2.system.TickAbilityDash
import cc.mewcraft.wakame.ability2.system.TickAbilityMultiJump
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
import cc.mewcraft.wakame.element.system.InitElementStackContainer
import cc.mewcraft.wakame.element.system.TickElementStack
import cc.mewcraft.wakame.enchantment2.system.ApplyEnchantmentEffect
import cc.mewcraft.wakame.enchantment2.system.TickAntigravShotEnchantment
import cc.mewcraft.wakame.enchantment2.system.TickAttributeEnchantment
import cc.mewcraft.wakame.enchantment2.system.TickBlastMiningEnchantment
import cc.mewcraft.wakame.enchantment2.system.TickFragileEnchantment
import cc.mewcraft.wakame.enchantment2.system.TickSmelterEnchantment
import cc.mewcraft.wakame.enchantment2.system.TickVeinminerEnchantment
import cc.mewcraft.wakame.entity.attribute.system.ApplyAttributeEffects
import cc.mewcraft.wakame.entity.attribute.system.InitAttributeContainer
import cc.mewcraft.wakame.entity.player.system.InitItemCooldownContainer
import cc.mewcraft.wakame.entity.player.system.PlayAttackSpeedAnimation
import cc.mewcraft.wakame.item2.ScanItemSlotChanges
import cc.mewcraft.wakame.item2.behavior.impl.weapon.SwitchKatana
import cc.mewcraft.wakame.item2.behavior.impl.weapon.TickKatana
import cc.mewcraft.wakame.kizami2.system.ApplyKizamiEffects
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
            add(RemoveBukkitEntities) // 移除无效 bukkit entity 所映射的 ecs entity
            add(RemoveBukkitBlocks) // 移除无效 bukkit block 所映射的 ecs entity

            add(InitAbilityContainer)
            add(InitItemCooldownContainer) // 初始化玩家的物品冷却容器
            add(InitAttributeContainer) // 初始化玩家的属性容器
            add(InitElementStackContainer)
            add(InitKizamiContainer) // 初始化玩家的铭刻容器
            add(InitMana)
            add(InitPlayerCombo) // 初始化玩家的连招状态
            add(ScanItemSlotChanges)// 监听玩家背包里的物品变化
            add(PlayAttackSpeedAnimation) // 渲染武器攻击速度的动画效果
            add(ApplyAttributeEffects) // 将物品上的属性效果应用到玩家
            add(ApplyKizamiEffects) // 将物品上的铭刻效果应用到玩家
            add(SwitchKatana) // 当玩家切换太刀时更新太刀状态
            add(TickKatana) // 更新每 tick 的太刀状态

            add(AbilityActivator) // “激活”玩家装备的技能
            add(AbilityRemover) // “移除”玩家装备的技能
            add(TickElementStack) // 元素特效层数
            add(UpdateEntityInfoBossBar) // 各种关于 boss bar 的逻辑
            add(ManageBossBar) // 显示/移除 boss bar
            add(CountTick) // 记录 entity 存在的 tick 数
            add(TickAbilityBlackhole)
            add(TickAbilityBlink)
            add(TickAbilityDash)
            add(TickAbilityMultiJump)
            add(ConsumeManaForAbilities) // 消耗使用技能的魔法值
            add(RenderOnceOffItemName)

            add(ApplyEnchantmentEffect) //
            add(TickAntigravShotEnchantment)
            add(TickAttributeEnchantment)
            add(TickBlastMiningEnchantment)
            add(TickFragileEnchantment)
            add(TickSmelterEnchantment)
            add(TickVeinminerEnchantment)
            add(UpdateMaxMana)
            add(RestoreMana)
            add(DisplayMana)
            add(RenderParticle)
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
