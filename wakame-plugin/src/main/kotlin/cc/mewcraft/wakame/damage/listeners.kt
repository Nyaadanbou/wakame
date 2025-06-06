package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import io.papermc.paper.event.entity.EntityKnockbackEvent
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.LinearComponents
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier.*
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import xyz.xenondevs.commons.provider.orElse

private val LOGGING by MAIN_CONFIG.optionalEntry<Boolean>("debug", "logging", "damage").orElse(false)

/**
 * 伤害系统的监听器, 也是游戏内伤害计算逻辑的代码入口.
 */
@Init(stage = InitStage.POST_WORLD)
internal object DamageListener : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    // 由于 MythicMobs 的各种问题, priority 必须设置为 MONITOR.
    // 整个项目中, Bukkit伤害事件应只在此处被监听
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: EntityDamageEvent) {
        val damagee = event.entity as? LivingEntity ?: return

        val damageContext = DamageContext(event)

        // 计算防御前的伤害
        // 考虑伤害发起者对伤害值的各种影响
        val damageMetadata = DamageManager.calculateDamageBeforeDefense(damageContext) ?: run {
            event.isCancelled = true
            return
        }

        // 计算防御后的伤害 (最终伤害)
        // 考虑伤害承受者对伤害值的各种影响
        val finalDamageMap = DamageManager.calculateFinalDamageMap(damageMetadata, damagee)

        val postprocessEvent = PostprocessDamageEvent(damageMetadata, finalDamageMap, event)
        if (!postprocessEvent.callEvent()) {
            // Koish 伤害事件被取消, 则直接返回
            // Koish 伤害事件被取消时, 其内部的 Bukkit 伤害事件必然是取消的状态
            return
        }

        // 修改原版各伤害修饰器
        event.modifyDamageModifiers()
        // 修改 Base 伤害
        event.modifyDamageModifier(BASE, postprocessEvent.getFinalDamage())

        // 记录日志
        if (LOGGING) {
            LOGGER.info("${damagee.type}(${damagee.uniqueId}) 受到了 ${event.damage} 点伤害")

            // 记录聊天
            // 2024/7/25 小米
            // 我承认, 我很复古.
            val message = LinearComponents.linear(
                translatable(damagee.type),
                if (damagee is Player) text(" ${damagee.name} ") else empty(),
                text("受到了"),
                text(" ${event.damage} "),
                text("点伤害")
            ).hoverEvent(
                damageMetadata.damageBundle.packets()
                    .map { packet -> LinearComponents.linear(packet.element.unwrap().displayName, text(": "), text(packet.packetDamage)) }
                    .join(JoinConfiguration.newlines())
                    .let { component ->
                        HoverEvent.showText(
                            component
                                .appendNewline()
                                .appendNewline()
                                .append(text("伤害未计算防御阶段"))
                                .appendNewline()
                                .append(text("点击复制实体 UUID"))
                        )
                    }
            ).clickEvent(
                ClickEvent.copyToClipboard(damagee.uniqueId.toString())
            )

            SERVER.filterAudience { it is Player }.sendMessage(message)
        }
    }

    /**
     * 方便函数.
     * 修改原版各伤害修饰器.
     */
    private fun EntityDamageEvent.modifyDamageModifiers(){
        // 以下修饰器移至 [DamageManager#calculateFinalDamage]
        // INVULNERABILITY_REDUCTION - 无懈可击时间伤害减免
        modifyDamageModifier(INVULNERABILITY_REDUCTION, .0)
        // BLOCKING - 格挡
        modifyDamageModifier(BLOCKING, .0)
        // RESISTANCE - 抗性提升药水效果伤害减免
        modifyDamageModifier(RESISTANCE, .0)
        // ABSORPTION - 伤害吸收
        modifyDamageModifier(ABSORPTION, .0)

        // 以下修饰器移除
        // FREEZING - 烈焰人等受细雪伤害*5
        // 该特性通过给相关生物配置对应元素易伤实现
        modifyDamageModifier(FREEZING, .0)
        // HARD_HAT - 头盔减少25%下落的方块伤害
        // 移除无关紧要的原版特性
        modifyDamageModifier(HARD_HAT, .0)
        // ARMOR - 原版盔甲值/盔甲韧性/魔咒效果组件armor_effectiveness的伤害减免
        // Koish 有专门的防御力机制
        modifyDamageModifier(ARMOR, .0)
        // MAGIC - 魔咒保护系数伤害减免/女巫85%魔法伤害减免
        // Koish 有专门的防御力机制
        modifyDamageModifier(MAGIC, .0)
    }

    /**
     * 方便函数.
     * 修改特定原版伤害修饰器.
     */
    private fun EntityDamageEvent.modifyDamageModifier(modifierType: EntityDamageEvent.DamageModifier, value: Double): Boolean {
        if (this.isApplicable(modifierType)) {
            this.setDamage(modifierType, value)
            return true
        }
        return false
    }

    @EventHandler
    fun on(event: ProjectileLaunchEvent) {
        DamageManager.registerTrident(event)
    }

    @EventHandler
    fun on(event: EntityShootBowEvent) {
        DamageManager.registerExactArrow(event)
    }

    // 在弹射物击中方块时移除记录的 DamageMetadata.
    @EventHandler
    fun on(event: ProjectileHitEvent) {
        if (event.hitBlock == null) return
        DamageManager.unregisterProjectile(event.entity)
    }

    // 用于取消自定义伤害的击退.
    @EventHandler
    fun on(event: EntityKnockbackEvent) {
        if (DamageManager.unregisterCancelKnockback(event.entity)) {
            event.isCancelled = true
        }
    }

}

/**
 * 监听 Koish 伤害事件, 使其遵循保护系统的规则.
 */
@Init(stage = InitStage.POST_WORLD)
internal object DamageIntegration : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PostprocessDamageEvent) {
        val damager = event.damageSource.causingEntity as? Player ?: return
        val damagee = event.damagee as? LivingEntity ?: return
        event.isCancelled = !ProtectionManager.canHurtEntity(damager, damagee, null)
    }

}