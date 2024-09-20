package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import io.papermc.paper.event.entity.EntityKnockbackEvent
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.LinearComponents
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Server
import org.bukkit.entity.AbstractArrow
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

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: EntityDamageEvent) {
        if (event.isCancelled) {
            return
        }

        val entity = event.entity
        if (entity !is LivingEntity) {
            return
        }

        val damageMetadata = DamageManager.generateDamageMetadata(event)
        val defenseMetadata = DamageManager.generateDefenseMetadata(event)

        val nekoEntityDamageEvent = NekoEntityDamageEvent(event.damageSource, damageMetadata, defenseMetadata, event)
        if (!nekoEntityDamageEvent.callEvent()) {
            event.isCancelled = true // 同时取消 EntityDamageEvent
            return
        }

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
                            .append(text("点击复制实体的 UUID"))
                    )
                }
        ).clickEvent(
            ClickEvent.copyToClipboard(entity.uniqueId.toString())
        )

        server.filterAudience { it is Player }.sendMessage(message)
    }

    /**
     * 在弹射物射出时记录其 [DamageMetadata].
     */
    @EventHandler
    fun on(event: ProjectileLaunchEvent) {
        DamageManager.recordProjectileDamageMetadata(event)
    }

    /**
     * 在弹射物射出时记录其 [DamageMetadata].
     *
     * 玩家射出的箭矢伤害需要根据拉弓的力度进行调整.
     */
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
        when (val projectile = event.entity) {
            // 弹射物是箭矢 (普通箭/光灵箭/药水箭) 和三叉戟
            is AbstractArrow -> {
                DamageManager.removeProjectileDamageMetadata(projectile.uniqueId)
            }
        }
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

    /* @EventHandler
    fun on(event: PlayerInteractEntityEvent) {
        val player = event.player
        val entity = event.rightClicked
        if (event.hand != EquipmentSlot.HAND) return
        player.sendMessage(Component.text("你右键了" + entity.type + "(" + entity.uniqueId + ")"))
        if (entity is LivingEntity) {
            entity.applyCustomDamage(
                CustomDamageMetadata(
                    1.0, false, false,
                    listOf(
                        ElementDamagePacket(ElementRegistry.DEFAULT, 5.0, 10.0, 0.0, 0.0, 0.0)
                    )
                ),
                player
            )
        }
    } */
}