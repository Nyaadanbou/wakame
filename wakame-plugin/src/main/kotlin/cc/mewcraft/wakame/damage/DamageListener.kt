package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.event
import io.papermc.paper.event.entity.EntityKnockbackEvent
import net.kyori.adventure.extra.kotlin.join
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.LinearComponents
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent

private val LOGGING by MAIN_CONFIG.entry<Boolean>("debug", "logging", "damage")

/**
 * 伤害系统的监听器, 也是代码入口.
 */
@Init(
    stage = InitStage.POST_WORLD,
)
internal object DamageListener {

    @InitFun
    fun init() {

        // 由于 MythicMobs 的各种问题, priority 必须设置为 MONITOR.
        event<EntityDamageEvent>(EventPriority.MONITOR, true) { event ->
            val entity = event.entity
            if (entity !is LivingEntity) {
                return@event
            }

            val damageMetadata = DamageManager.generateDamageMetadata(event)
            if (damageMetadata == null) {
                event.isCancelled = true
                return@event
            }

            val defenseMetadata = DamageManager.generateDefenseMetadata(event)
            val nekoEntityDamageEvent = NekoEntityDamageEvent(damageMetadata, defenseMetadata, event)

            // 萌芽伤害事件被取消, 则直接返回
            // 萌芽伤害事件被取消时, 其内部的 Bukkit 伤害事件必然是取消的状态
            if (!nekoEntityDamageEvent.callEvent()) {
                return@event
            }

            // 修改最终伤害
            event.damage = nekoEntityDamageEvent.getFinalDamage()

            // 记录日志
            if (LOGGING) {
                LOGGER.info("${entity.type}(${entity.uniqueId}) 受到了 ${event.damage} 点伤害")

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
                        .map { packet -> LinearComponents.linear(packet.element.displayName, text(": "), text(packet.damageValue())) }
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
                    ClickEvent.copyToClipboard(entity.uniqueId.toString())
                )

                SERVER.sendMessage(message)
            }
        }

        event<ProjectileLaunchEvent> {
            DamageManager.recordProjectileDamageMetadata(it)
        }

        event<EntityShootBowEvent> {
            DamageManager.recordProjectileDamageMetadata(it)
        }

        // 在弹射物击中方块时移除记录的 DamageMetadata.
        event<ProjectileHitEvent> {
            if (it.hitBlock == null) return@event
            DamageManager.removeProjectileDamageMetadata(it.entity.uniqueId)
        }

        // 用于取消自定义伤害的击退.
        event<EntityKnockbackEvent> {
            val uuid = it.entity.uniqueId
            if (DamageManager.unmarkCancelKnockback(uuid)) {
                it.isCancelled = true
            }
        }
    }

}