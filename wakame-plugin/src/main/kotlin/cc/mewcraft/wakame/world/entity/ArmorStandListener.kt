@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.world.entity

import cc.mewcraft.wakame.adventure.translator.MessageConstants
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.util.ui.whisper
import me.lucko.helper.metadata.MetadataKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class ArmorStandListener : Listener {
    companion object {
        private val MESSAGE_COOLDOWN = MetadataKey.createCooldownKey("sneak_to_break_armor_stand_tip")
    }

    private val requireSneakingToBreakArmorStand: Boolean by MAIN_CONFIG.entry("require_sneaking_to_break_armor_stand")

    @EventHandler
    fun on(event: EntityDeathEvent) {
        val damagee = event.entity as? ArmorStand ?: return
        val damager = event.damageSource.causingEntity as? Player ?: return
        if (requireSneakingToBreakArmorStand && !damager.isSneaking) {
            event.isCancelled = true
            whisper(damager, 10, MESSAGE_COOLDOWN) { sendMessage(MessageConstants.MSG_SNEAK_TO_BREAK_ARMOR_STAND) }
        }
    }
}