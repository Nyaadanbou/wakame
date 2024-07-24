package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.event.WakameEntityDamageEvent
import io.papermc.paper.event.entity.EntityKnockbackEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

object DamageListener : Listener, KoinComponent {
    private val logger: Logger by inject()

    @EventHandler
    fun on(event: EntityDamageEvent) {
        if (event.entity !is LivingEntity) return
        val damageMetaData = DamageManager.generateDamageMetaData(event)
        val defenseMetaData = DamageManager.generateDefenseMetaData(event)
        val wakameEntityDamageEvent = WakameEntityDamageEvent(damageMetaData, defenseMetaData)
        wakameEntityDamageEvent.callEvent()

        // 如果伤害事件被取消，什么也不做
        if (wakameEntityDamageEvent.isCancelled) return


        // 修改最终伤害
        event.damage = wakameEntityDamageEvent.finalDamage
        logger.info("${event.entity.type}(${event.entity.uniqueId}) 受到了 ${event.damage} 点伤害")
        val stringBuilder = StringBuilder()
        val miniMessage = MiniMessage.miniMessage()
        for (it in damageMetaData.damageBundle.packets()) {
            stringBuilder.append("${miniMessage.serialize(it.element.displayName)}: ${it.packetDamage}<newline>")
        }
        stringBuilder.append("（各元素未计算防御阶段）")
        Bukkit.broadcast(
            miniMessage.deserialize("<hover:show_text:'${stringBuilder}'>${event.entity.type}(${event.entity.uniqueId}) 受到了 ${event.damage} 点伤害")
        )
    }

    /**
     * 在弹射物射出时记录其 [DamageMetadata]
     */
    @EventHandler
    fun on(event: ProjectileLaunchEvent) {
        DamageManager.recordProjectileDamageMetadata(event)
    }

    /**
     * 在弹射物射出时记录其 [DamageMetadata]
     * 玩家射出的箭矢伤害需要根据拉弓的力度进行调整
     */
    @EventHandler
    fun on(event: EntityShootBowEvent) {
        DamageManager.recordProjectileDamageMetadata(event)
    }

    /**
     * 在弹射物击中方块时移除记录的 [DamageMetadata]
     */
    @EventHandler
    fun on(event: ProjectileHitEvent) {
        if (event.hitBlock == null) {
            return
        }
        when (val projectile = event.entity) {
            // 弹射物是箭矢（普通箭、光灵箭、药水箭）、三叉戟
            is AbstractArrow -> {
                DamageManager.removeProjectileDamageMetadata(projectile.uniqueId)
            }

            // TODO 可能还会有其他需要wakame属性系统处理的弹射物
        }
    }

    /**
     * 用于取消自定义伤害的击退
     */
    @EventHandler
    fun on(event: EntityKnockbackEvent) {
        val uuid = event.entity.uniqueId
        val customDamageMetaData = DamageManager.findCustomDamageMetadata(uuid) ?: return
        if (!customDamageMetaData.knockback) {
            event.isCancelled = true
        }
        DamageManager.removeCustomDamageMetadata(uuid)
    }

//    @EventHandler
//    fun on(event: PlayerInteractEntityEvent) {
//        val player = event.player
//        val entity = event.rightClicked
//        if (event.hand != EquipmentSlot.HAND) return
//        player.sendMessage(Component.text("你右键了" + entity.type + "(" + entity.uniqueId + ")"))
//        if (entity is LivingEntity) {
//            entity.applyCustomDamage(
//                CustomDamageMetaData(
//                    1.0, false, false,
//                    listOf(
//                        ElementDamagePacket(ElementRegistry.DEFAULT, 5.0, 10.0, 0.0, 0.0, 0.0)
//                    )
//                ),
//                player
//            )
//        }
//    }
}