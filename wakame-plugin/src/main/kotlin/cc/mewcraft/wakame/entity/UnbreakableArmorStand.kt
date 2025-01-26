package cc.mewcraft.wakame.entity

import cc.mewcraft.wakame.adventure.translator.MessageConstants
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.util.event
import cc.mewcraft.wakame.util.ui.whisper
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import me.lucko.helper.metadata.MetadataKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

/**
 * 阻止玩家在非潜行状态下破坏盔甲架.
 */
@Init(
    stage = InitStage.POST_WORLD,
)
internal object UnbreakableArmorStand {

    @InitFun
    fun init() {
        registerListeners()
    }

    private fun registerListeners() {
        // 首先在 ProjectileHitEvent 将盔甲架上面的物品保存下来,
        // 然后在 EntityDeathEvent 里面恢复在这里保存的物品.
        event<ProjectileHitEvent>(ignoreCancelled = true){
            val armorStand = it.hitEntity as? ArmorStand ?: return@event
            val player = it.entity.shooter as? Player ?: return@event
            if (requireSneakingToBreakArmorStand && !player.isSneaking) {
                val stored = getItems(armorStand)
                itemStacksOnArmorStands.put(armorStand, stored)
            }
        }

        // 在 EntityDeathEvent 里面恢复在 ProjectileHitEvent 里面保存的物品.
        event<EntityDeathEvent>(ignoreCancelled = true){
            val armorStand = it.entity as? ArmorStand ?: return@event
            val player = it.damageSource.causingEntity as? Player ?: return@event
            if (requireSneakingToBreakArmorStand && !player.isSneaking) {
                // 事件取消后, 盔甲架不会消失, 但上面的装备会全部消失.
                // 读取之前保存的物品, 手动恢复盔甲架上面的所有物品.
                val stored = itemStacksOnArmorStands.getIfPresent(armorStand) ?: return@event
                setItems(armorStand, stored)

                // 取消死亡事件, 使盔甲架不消失.
                it.isCancelled = true

                // 提示玩家潜行后才能破坏盔甲架.
                whisper(player, 10, MESSAGE_COOLDOWN) { sendMessage(MessageConstants.MSG_SNEAK_TO_BREAK_ARMOR_STAND) }
            }
        }
    }

    private val MESSAGE_COOLDOWN = MetadataKey.createCooldownKey("sneak_to_break_armor_stand_tip")

    // config
    private val requireSneakingToBreakArmorStand: Boolean by MAIN_CONFIG.entry<Boolean>("require_sneaking_to_break_armor_stand")

    // aux data
    private val itemStacksOnArmorStands: Cache<ArmorStand, Map<EquipmentSlot, ItemStack>> =
        Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build()

    private fun getItems(armorStand: ArmorStand): Map<EquipmentSlot, ItemStack> {
        return EquipmentSlot.entries
            .filter { slot -> slot != EquipmentSlot.BODY }
            .associateWith(armorStand::getItem)
    }

    private fun setItems(armorStand: ArmorStand, items: Map<EquipmentSlot, ItemStack>) {
        items.forEach { (slot, item) -> armorStand.setItem(slot, item) }
    }
}