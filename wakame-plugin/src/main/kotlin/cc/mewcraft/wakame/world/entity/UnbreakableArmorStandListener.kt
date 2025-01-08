@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.world.entity

import cc.mewcraft.wakame.adventure.translator.MessageConstants
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.util.ui.whisper
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import me.lucko.helper.metadata.MetadataKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

/**
 * 阻止玩家在非潜行状态下破坏盔甲架.
 */
class UnbreakableArmorStandListener : Listener {
    companion object {
        private val MESSAGE_COOLDOWN = MetadataKey.createCooldownKey("sneak_to_break_armor_stand_tip")
    }

    // config
    private val requireSneakingToBreakArmorStand: Boolean by MAIN_CONFIG.entry("require_sneaking_to_break_armor_stand")

    // aux data
    private val cache: Cache<ArmorStand, Map<EquipmentSlot, ItemStack>> = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build()

    private fun getItems(armorStand: ArmorStand): Map<EquipmentSlot, ItemStack> {
        return EquipmentSlot.entries
            .filter { slot -> slot != EquipmentSlot.BODY }
            .associateWith(armorStand::getItem)
    }

    private fun setItems(armorStand: ArmorStand, items: Map<EquipmentSlot, ItemStack>) {
        items.forEach { (slot, item) -> armorStand.setItem(slot, item) }
    }

    // 首先在 ProjectileHitEvent 将盔甲架上面的物品保存下来,
    // 然后在 EntityDeathEvent 里面恢复在这里保存的物品.
    @EventHandler(ignoreCancelled = true)
    fun on(event: ProjectileHitEvent) {
        val armorStand = event.hitEntity as? ArmorStand ?: return
        val player = event.entity.shooter as? Player ?: return
        if (requireSneakingToBreakArmorStand && !player.isSneaking) {
            val stored = getItems(armorStand)
            cache.put(armorStand, stored)
        }
    }

    // 在 EntityDeathEvent 里面恢复在 ProjectileHitEvent 里面保存的物品.
    @EventHandler(ignoreCancelled = true)
    fun on(event: EntityDeathEvent) {
        val armorStand = event.entity as? ArmorStand ?: return
        val player = event.damageSource.causingEntity as? Player ?: return
        if (requireSneakingToBreakArmorStand && !player.isSneaking) {
            // 事件取消后, 盔甲架不会消失, 但上面的装备会全部消失.
            // 读取之前保存的物品, 手动恢复盔甲架上面的所有物品.
            val stored = cache.getIfPresent(armorStand) ?: return
            setItems(armorStand, stored)

            // 取消死亡事件, 使盔甲架不消失.
            event.isCancelled = true

            // 提示玩家潜行后才能破坏盔甲架.
            whisper(player, 10, MESSAGE_COOLDOWN) { sendMessage(MessageConstants.MSG_SNEAK_TO_BREAK_ARMOR_STAND) }
        }
    }
}