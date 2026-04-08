package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.item.ItemRef
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
fun Player.removeItem(removeItems: Map<ItemRef, Int>): Map<ItemRef, Int> {
    val map = removeItems.toMutableMap()
    for (playerItemStack in inventory) {
        if (playerItemStack == null || playerItemStack.isEmpty) {
            continue
        }

        val iterator = map.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            val removeItemRef = next.key
            val removeAmount = next.value

            if (removeItemRef.matches(playerItemStack)) {
                if (playerItemStack.amount >= removeAmount) {
                    playerItemStack.amount -= removeAmount
                    iterator.remove()
                } else {
                    map[removeItemRef] = removeAmount - playerItemStack.amount
                    playerItemStack.amount = 0
                }

                // 因为传入的是 Map<ItemRef, Int>, ItemRef 作为键有唯一性,
                // 即 ItemRef 最多只有其中一个会和遍历到的 playerItemStack 匹配上,
                // 所以对上了就直接 break
                break
            }
        }
    }
    return map
}
