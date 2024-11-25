package cc.mewcraft.wakame.reforge.repair

import cc.mewcraft.wakame.integration.economy.EconomyManager
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.reforge.common.PriceInstance
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.util.damage
import cc.mewcraft.wakame.util.decorate
import cc.mewcraft.wakame.util.isDamageable
import cc.mewcraft.wakame.util.isDamaged
import cc.mewcraft.wakame.util.itemName
import cc.mewcraft.wakame.util.plain
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger

internal class SimpleRepairingSession(
    override val table: RepairingTable,
    override val viewer: Player,
) : RepairingSession, KoinComponent {
    val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.RECYCLE)

    // 储存了当前所有的 claim.
    // 这里的 index 就是它显示在修理菜单里的位置 (display slot).
    private val claims: ArrayList<Claim> = ArrayList()

    private fun getItemKey(item: ItemStack): Key {
        // 先尝试获取萌芽系统里的物品 id,
        // 如果没有的话就返回原版物品 id
        return item.shadowNeko()?.id ?: item.type.key
    }

    private fun getItemPrice(item: ItemStack?): PriceInstance? {
        if (item?.isDamaged == true) {
            // 首先物品堆叠必须是受损的,
            // 未受损的物品无需修复,
            // 因此也没有价格.
            val id = getItemKey(item)
            return table.getPrice(id)
        } else {
            // 否则直接返回 null
            return null
        }
    }

    private fun checkIndex(index: Int): Boolean {
        return index in 0 until claims.size
    }

    override fun getClaim(displaySlot: Int): RepairingSession.Claim? {
        if (!checkIndex(displaySlot)) {
            return null
        }

        return claims[displaySlot]
    }

    override fun removeClaim(displaySlot: Int): RepairingSession.Claim? {
        if (!checkIndex(displaySlot)) {
            return null
        }

        return claims.removeAt(displaySlot)
    }

    override fun clearClaims() {
        claims.clear()
    }

    override fun getAllClaims(): List<RepairingSession.Claim> {
        return claims
    }

    // 找出背包里所有可修复的物品堆叠,
    // 然后把它们加入到 claims 列表中.
    override fun registerClaims(inventory: PlayerInventory) {
        // 清空旧的, 因为背包可能已经发生变化
        claims.clear()

        // 遍历背包, 找出所有需要修复的物品
        for (itemStack /* mirror */ in inventory.storageContents) {
            if (itemStack == null) {
                continue
            }

            val price = getItemPrice(itemStack) ?: continue

            val costValue = price.getValue(itemStack)
            val repairCost = RepairCost(costValue)

            claims += Claim(
                repairCost = repairCost,
                originalItem = itemStack
            )
        }
    }


    ////// inner class //////


    /**
     * @param originalItem 需要修复的物品 (直接引用)
     * @param repairCost 修复需要的花费
     */
    inner class Claim(
        override val repairCost: RepairingSession.RepairCost,
        override val originalItem: ItemStack,
    ) : RepairingSession.Claim {
        override val displaySlot: Int
            get() = claims.indexOf(this)

        override fun repair(player: Player) {
            // 菜单里应该已经检查 repairCost, 这里直接跳过

            if (originalItem.isDamageable) {
                // 再次检查是否为 damageable

                // 修复物品
                originalItem.damage = 0

                logger.info("Repaired item: ${originalItem.itemName?.plain ?: originalItem.type}")
            } else {
                logger.error("Repairing a non-damageable item. This is a bug!")
                logger.error("The non-damageable item is logged below:")
                logger.error(originalItem.toString())
            }
        }
    }

    /**
     * @param value 修复需要花费的金币
     */
    inner class RepairCost(
        override val value: Double,
    ) : RepairingSession.RepairCost {
        override fun test(player: Player): Boolean {
            return EconomyManager.has(player.uniqueId, value).getOrDefault(false)
        }

        override fun take(player: Player) {
            EconomyManager.take(player.uniqueId, value).getOrDefault(false)
        }
    }
}