package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.item2.ItemRef
import io.papermc.paper.adventure.PaperAdventure
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.max

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

/**
 * 获取玩家的 [id] 冷却组是否正在冷却.
 */
fun Player.isItemCooldownActive(id: Identifier): Boolean {
    return getItemCooldownPercent(id) > 0f
}

/**
 * 获取玩家的 [id] 冷却组的 *未完成冷却时长* 占 *总冷却时长* 的比例.
 */
fun Player.getItemCooldownPercent(id: Identifier): Float {
    val resLoc = PaperAdventure.asVanilla(id)
    val cooldownMap = serverPlayer.cooldowns
    val cooldownInst = cooldownMap.cooldowns[resLoc]
    if (cooldownInst != null) {
        val f = cooldownInst.endTime - cooldownInst.startTime
        val g = cooldownInst.endTime - cooldownMap.tickCount
        return (g.toFloat() / f.toFloat()).coerceIn(0f, 1f)
    } else {
        return 0f
    }
}

/**
 * 获取玩家的 [id] 冷却组的剩余冷却时间, 单位: tick.
 */
fun Player.getItemCooldownRemainingTicks(id: Identifier): Int {
    val resLoc = PaperAdventure.asVanilla(id)
    val cooldownMap = serverPlayer.cooldowns
    val cooldownInst = cooldownMap.cooldowns[resLoc]
    if (cooldownInst != null) {
        return max(cooldownInst.endTime - cooldownMap.tickCount, 0)
    } else {
        return 0
    }
}

/**
 * 使玩家的 [id] 冷却组进入冷却.
 */
fun Player.addItemCooldown(id: Identifier, ticks: Int) {
    val resLoc = PaperAdventure.asVanilla(id)
    this.serverPlayer.cooldowns.addCooldown(resLoc, ticks, true)
}

/**
 * 使玩家的 [id] 冷却组结束冷却.
 */
fun Player.removeItemCooldown(id: Identifier) {
    val resLoc = PaperAdventure.asVanilla(id)
    this.serverPlayer.cooldowns.removeCooldown(resLoc)
}