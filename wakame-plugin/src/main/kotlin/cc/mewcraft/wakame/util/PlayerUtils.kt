package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.core.ItemX
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * 向玩家背包中添加物品.
 * 物品会尽可能的堆叠.
 * 添加失败的物品会自动掉在地上.
 */
fun Player.giveItemStack(vararg itemStacks: ItemStack?) {
    for (itemStack in itemStacks) {
        if (itemStack == null || itemStack.isEmpty)
            continue
        val item = location.world.dropItemNaturally(location, itemStack)
        item.owner = uniqueId
        item.pickupDelay = 0
    }
}

/**
 * 移除玩家背包中的物品.
 * 返回未成功移除的物品.
 */
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