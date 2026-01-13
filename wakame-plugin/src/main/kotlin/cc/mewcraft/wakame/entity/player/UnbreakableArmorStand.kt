package cc.mewcraft.wakame.entity.player

import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.entry
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.metadata.MetadataKey
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.ui.whisper
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
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
@Init(InitStage.POST_WORLD)
internal object UnbreakableArmorStand : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    // 首先在 ProjectileHitEvent 将盔甲架上面的物品保存下来,
    // 然后在 EntityDeathEvent 里面恢复在这里保存的物品.
    @EventHandler(ignoreCancelled = true)
    fun on(event: ProjectileHitEvent) {
        val armorStand = event.hitEntity as? ArmorStand ?: return
        val player = event.entity.shooter as? Player ?: return
        if (requireSneakingToBreakArmorStand && !player.isSneaking) {
            val stored = getItems(armorStand)
            itemStacksOnArmorStands.put(armorStand, stored)
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
            val stored = itemStacksOnArmorStands.getIfPresent(armorStand) ?: return
            setItems(armorStand, stored)

            // 取消死亡事件, 使盔甲架不消失.
            event.isCancelled = true

            // 提示玩家潜行后才能破坏盔甲架.
            whisper(player, 10, MESSAGE_COOLDOWN) { sendMessage(TranslatableMessages.MSG_SNEAK_TO_BREAK_ARMOR_STAND) }
        }
    }

    private val MESSAGE_COOLDOWN = MetadataKey.createCooldownKey("sneak_to_break_armor_stand_tip")

    // config
    private val requireSneakingToBreakArmorStand: Boolean by MAIN_CONFIG.entry<Boolean>("require_sneaking_to_break_armor_stand")

    // aux data
    private val itemStacksOnArmorStands: Cache<ArmorStand, Map<EquipmentSlot, ItemStack>> =
        CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build()

    private fun getItems(armorStand: ArmorStand): Map<EquipmentSlot, ItemStack> {
        return EquipmentSlot.entries
            .filter { slot -> slot != EquipmentSlot.BODY }
            .associateWith(armorStand::getItem)
    }

    private fun setItems(armorStand: ArmorStand, items: Map<EquipmentSlot, ItemStack>) {
        items.forEach { (slot, item) -> armorStand.setItem(slot, item) }
    }
}