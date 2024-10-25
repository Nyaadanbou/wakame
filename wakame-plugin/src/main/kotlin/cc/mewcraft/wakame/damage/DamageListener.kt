package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import io.papermc.paper.event.entity.EntityKnockbackEvent
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.LinearComponents
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Server
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

/**
 * 伤害系统的监听器, 也是代码入口.
 */
object DamageListener : Listener, KoinComponent {
    private val logger: Logger by inject()
    private val server: Server by inject()

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: EntityDamageEvent) {
        val entity = event.entity
        if (entity !is LivingEntity) {
            return
        }

        val damageMetadata = DamageManager.generateDamageMetadata(event)
        if (damageMetadata == null) {
            event.isCancelled = true
            return
        }
        val defenseMetadata = DamageManager.generateDefenseMetadata(event)

        val nekoEntityDamageEvent = NekoEntityDamageEvent(damageMetadata, defenseMetadata, event)
        // neko伤害事件被取消, 则直接返回
        // neko伤害事件被取消时，其内部的Bukkit伤害事件必然是取消的状态
        if (!nekoEntityDamageEvent.callEvent()) return

        // 修改最终伤害
        event.damage = nekoEntityDamageEvent.getFinalDamage()

        // 记录日志
        logger.info("${entity.type}(${entity.uniqueId}) 受到了 ${event.damage} 点伤害")

        // 记录聊天
        // 2024/7/25 小米
        // 我承认, 我很复古.
        val message = LinearComponents.linear(
            translatable(entity.type),
            if (entity is Player) text(" ${entity.name} ") else empty(),
            text("受到了"),
            text(" ${event.damage} "),
            text("点伤害")
        ).hoverEvent(
            damageMetadata.damageBundle.packets()
                .map { packet -> LinearComponents.linear(packet.element.displayName, text(": "), text(packet.packetDamage)) }
                .let { components -> join(JoinConfiguration.newlines(), components) }
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
            ClickEvent.copyToClipboard(entity.uniqueId.toString())
        )

        server.filterAudience { it is Player }.sendMessage(message)
    }


    @EventHandler
    fun on(event: ProjectileLaunchEvent) {
        DamageManager.recordProjectileDamageMetadata(event)
    }


    @EventHandler
    fun on(event: EntityShootBowEvent) {
        DamageManager.recordProjectileDamageMetadata(event)
    }

    /**
     * 在弹射物击中方块时移除记录的 [DamageMetadata].
     */
    @EventHandler
    fun on(event: ProjectileHitEvent) {
        if (event.hitBlock == null) {
            return
        }
        DamageManager.removeProjectileDamageMetadata(event.entity.uniqueId)
    }

    /**
     * 用于取消自定义伤害的击退.
     */
    @EventHandler
    fun on(event: EntityKnockbackEvent) {
        val uuid = event.entity.uniqueId
        if (DamageManager.unmarkCancelKnockback(uuid)) {
            event.isCancelled = true
        }
    }
}