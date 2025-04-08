package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.core.ItemX
import org.bukkit.entity.Player

/**
 * 移除玩家背包中的物品.
 * 返回未成功移除的物品.
 */
@Deprecated("请使用 ItemRef 的版本")
fun Player.removeItem(removeItems: Map<ItemX, Int>): Map<ItemX, Int> {
    val player = this
    val map = removeItems.toMutableMap()
    for (playerItemStack in player.inventory) {
        if (playerItemStack == null || playerItemStack.isEmpty) continue

        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val removeItemX = next.key
            val removeAmount = next.value

            if (removeItemX.matches(playerItemStack)) {
                if (playerItemStack.amount >= removeAmount) {
                    playerItemStack.amount -= removeAmount
                    iterator.remove()
                } else {
                    map[removeItemX] = removeAmount - playerItemStack.amount
                    playerItemStack.amount = 0
                }
                // 因为传入的是Map<ItemX, Int>, ItemX作为键有唯一性
                // 即ItemX最多只有其中一个会和遍历到的playerItemStack匹配上
                // 所以对上了就直接break
                break
            }
        }
    }
    return map
}