package cc.mewcraft.wakame.kizami.system

import cc.mewcraft.wakame.entity.player.OnlineUserTicker
import cc.mewcraft.wakame.entity.player.User
import cc.mewcraft.wakame.item.ItemStackEffectiveness
import cc.mewcraft.wakame.item.extension.kizamiz
import cc.mewcraft.wakame.item.forEachChangingEntry
import org.bukkit.entity.Player

/**
 * 使物品上的铭刻对玩家生效.
 */
object ItemInscriptionSystem : OnlineUserTicker {
    override fun onTickUser(user: User, player: Player) {
        val itemSlotChanges = user.itemSlotChanges
        val liveKizamiContainer = user.inscriptionContainer
        // 优化: 保存变化前的铭刻状态, 用作之后铭刻效果的移除
        val prevKizamiContainer = liveKizamiContainer.copy()
        // 更新铭刻的数量
        var changed = false
        itemSlotChanges.forEachChangingEntry { slot, curr, prev ->
            if (prev != null && ItemStackEffectiveness.testSlot(slot, prev)) {
                val kizami = prev.kizamiz
                if (kizami.isNotEmpty()) {
                    liveKizamiContainer.subtractOneEach(kizami)
                    changed = true
                }
            }
            if (curr != null &&
                ItemStackEffectiveness.testSlot(slot, curr) &&
                ItemStackEffectiveness.testLevel(player, curr) &&
                ItemStackEffectiveness.testDamaged(curr)
            ) {
                val kizami = curr.kizamiz
                if (kizami.isNotEmpty()) {
                    liveKizamiContainer.addOneEach(kizami)
                    changed = true
                }
            }
        }
        // 更新铭刻的效果
        if (changed) {
            prevKizamiContainer.removeAllEffects(player)
            liveKizamiContainer.applyAllEffects(player)
        }
    }
}