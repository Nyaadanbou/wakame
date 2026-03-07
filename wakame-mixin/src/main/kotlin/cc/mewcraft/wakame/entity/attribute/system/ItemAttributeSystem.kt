package cc.mewcraft.wakame.entity.attribute.system

import cc.mewcraft.wakame.entity.player.OnlineUserTicker
import cc.mewcraft.wakame.entity.player.User
import cc.mewcraft.wakame.entity.player.UserManager
import cc.mewcraft.wakame.item.ItemStackEffectiveness
import cc.mewcraft.wakame.item.extension.coreContainer
import cc.mewcraft.wakame.item.forEachChangingEntry
import org.bukkit.entity.Player

/**
 * 使物品上的属性对玩家生效.
 */
object ItemAttributeSystem : OnlineUserTicker {
    override fun onTickUser(user: User, player: Player) {
        val user = UserManager.get(player)
        val itemSlotChanges = user.itemSlotChanges
        val attributeContainer = user.attributeContainer
        itemSlotChanges.forEachChangingEntry { slot, curr, prev ->
            if (prev != null && ItemStackEffectiveness.testSlot(slot, prev)) {
                val coreContainer = prev.coreContainer
                if (coreContainer != null) {
                    val attrModifiers = coreContainer.collectAttributeModifiers(prev, slot)
                    attributeContainer.removeModifiers(attrModifiers)
                }
            }
            if (curr != null &&
                ItemStackEffectiveness.testSlot(slot, curr) &&
                ItemStackEffectiveness.testLevel(player, curr) &&
                ItemStackEffectiveness.testDamaged(curr)
            ) {
                val coreContainer = curr.coreContainer
                if (coreContainer != null) {
                    val attrModifiers = coreContainer.collectAttributeModifiers(curr, slot)
                    attributeContainer.addTransientModifiers(attrModifiers)
                }
            }
        }
    }
}