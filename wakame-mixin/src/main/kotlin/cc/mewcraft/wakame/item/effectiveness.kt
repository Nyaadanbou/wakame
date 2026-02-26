package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.entity.player.koishLevel
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelUpEvent
import cc.mewcraft.wakame.item.extension.level
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.ItemSlot
import cc.mewcraft.wakame.item.property.impl.ItemSlotGroup
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.toNMS
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

object ItemStackEffectivenessListener : Listener {

    @EventHandler
    fun on(event: PlayerLevelUpEvent) {
        // 当玩家冒险等级升级时, 更新背包内的所有物品
        val player = event.player
        player.updateInventory()
    }
}

/**
 * 用于判断玩家手中的物品堆叠是否生效.
 */
object ItemStackEffectiveness {

    /**
     * 检查物品在设定的生效槽位.
     */
    fun testSlot(slot: ItemSlot, itemstack: MojangStack?): Boolean {
        if (itemstack == null) {
            return false
        }
        val slotGroup = itemstack.getProp(ItemPropTypes.SLOT) ?: ItemSlotGroup.empty()
        return slotGroup.contains(slot)
    }

    /**
     * 检查物品在设定的生效槽位.
     */
    fun testSlot(slot: ItemSlot, itemstack: ItemStack?): Boolean {
        return testSlot(slot, itemstack?.toNMS()) //
    }

    /**
     * 检查物品在设定的生效槽位.
     *
     * 关于 [convertedSlot] 的取值参考:
     *
     * Converted Slots:
     * ```
     * 39             1  2     0
     * 38             3  4
     * 37
     * 36          40
     * 9  10 11 12 13 14 15 16 17
     * 18 19 20 21 22 23 24 25 26
     * 27 28 29 30 31 32 33 34 35
     * 0  1  2  3  4  5  6  7  8
     * ```
     */
    fun testSlot(convertedSlot: Int, itemstack: MojangStack?): Boolean {
        if (itemstack == null) {
            return false
        }
        val slotGroup = itemstack.getProp(ItemPropTypes.SLOT) ?: ItemSlotGroup.empty()
        return slotGroup.contains(convertedSlot)
    }

    /**
     * 检查物品的等级小于等于玩家的冒险等级.
     */
    fun testLevel(player: Player, itemstack: MojangStack?): Boolean {
        if (itemstack == null) {
            return true // 非 Koish 物品 - 视为玩家的等级高于该物品的等级
        }
        val itemLevel = itemstack.level?.level
        if (itemLevel == null) {
            return true // 物品没有等级 - 视为玩家的等级高于该物品的等级
        }
        val playerLevel = player.koishLevel
        return itemLevel <= playerLevel
    }

    /**
     * 检查物品的等级小于等于玩家的冒险等级.
     */
    fun testLevel(player: Player, itemstack: ItemStack?): Boolean {
        return testLevel(player, itemstack?.toNMS())
    }

    /**
     * 检查物品没有损坏.
     */
    fun testDamaged(itemstack: MojangStack?): Boolean {
        if (itemstack == null) {
            return true // 非 Koish 物品 - 视为没有耐久度, 应该返回 false
        }
        if (!itemstack.isDamageableItem) {
            return true // 如果物品有“无法破坏”或耐久组件不完整, 那么认为物品没有耐久度, 应该返回 true
        }
        if (itemstack.damageValue >= itemstack.maxDamage) {
            return false // 如果物品已经损坏, 那么应该返回 false
        }
        return true
    }

    /**
     * 检查物品没有损坏.
     */
    fun testDamaged(itemstack: ItemStack?): Boolean {
        return testDamaged(itemstack?.toNMS())
    }
}