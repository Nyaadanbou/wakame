package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.event.bukkit.NekoPostprocessEntityDamageEvent
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
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent

private val LOGGING by MAIN_CONFIG.entry<Boolean>("debug", "logging", "damage")

/**
 * 伤害系统的监听器, 也是代码入口.
 */
@Init(stage = InitStage.POST_WORLD)
internal object DamageListener : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    // 由于 MythicMobs 的各种问题, priority 必须设置为 MONITOR.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: EntityDamageEvent) {
        val damagee = event.entity as? LivingEntity ?: return

        // 计算最终伤害
        val damageContext = DamageContext(event)
        val damageMetadata = DamageManager.calculateDamageBeforeDefense(damageContext) ?: run {
            event.isCancelled = true
            return
        }
        val finalDamageMap = DamageManager.calculateFinalDamageMap(damageMetadata, damagee)
        val postprocessEvent = NekoPostprocessEntityDamageEvent(damageMetadata, finalDamageMap, event)
        if (!postprocessEvent.callEvent()) {
            // 萌芽伤害事件被取消, 则直接返回
            // 萌芽伤害事件被取消时, 其内部的 Bukkit 伤害事件必然是取消的状态
            return
        }

        // 修改最终伤害
        event.damage = postprocessEvent.getFinalDamage()

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
                    .map { packet -> LinearComponents.linear(packet.element.value.displayName, text(": "), text(packet.packetDamage)) }
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

            SERVER.sendMessage(message)
        }
    }

    @EventHandler
    fun on(event: ProjectileLaunchEvent) {
        DamageManager.registerProjectile(event)
    }

    @EventHandler
    fun on(event: EntityShootBowEvent) {
        DamageManager.registerProjectile(event)
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
 * 监听萌芽伤害事件, 使其遵循保护系统的规则.
 */
@Init(stage = InitStage.POST_WORLD)
internal object DamageIntegration : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: NekoPostprocessEntityDamageEvent) {
        val damager = event.damageSource.causingEntity as? Player ?: return
        val damagee = event.damagee as? LivingEntity ?: return
        event.isCancelled = !ProtectionManager.canHurtEntity(damager, damagee, null)
    }

}