package cc.mewcraft.wakame.item.feature

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.hasProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.runTaskLater
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.PlayerInventory

/**
 * 实现了修复石机制.
 *
 * 玩家在背包里拿起修复石, 然后跟想要修复的装备交换位置, 即可完成修复.
 * 修复石有两种类型:
 * - 固定修复: 修复固定的耐久度
 * - 百分比修复: 按最大耐久的百分比修复
 */
@Init(InitStage.POST_WORLD)
object RepairStone : Listener {

    init {
        registerEvents()
    }

    @EventHandler
    private fun on(event: InventoryClickEvent) {
        handleRepair(event)
    }

    private fun handleRepair(event: InventoryClickEvent) {
        if (event.isCancelled) return
        if (!event.isRightClick && !event.isLeftClick) return
        val clickedInventory = event.clickedInventory as? PlayerInventory ?: return
        val player = event.whoClicked as? Player ?: return
        if (player.gameMode == GameMode.CREATIVE) return

        val repairStoneItem = event.cursor.takeUnlessEmpty() ?: return
        val targetItem = event.currentItem?.takeUnlessEmpty() ?: return

        // 修复石必须拥有 REPAIR_STONE 属性
        val repairStoneData = repairStoneItem.getProp(ItemPropTypes.REPAIR_STONE) ?: return

        // 目标物品必须拥有 REPAIRABLE_BY_REPAIR_STONE 标记
        if (!targetItem.hasProp(ItemPropTypes.REPAIRABLE_BY_REPAIR_STONE)) {
            player.sendMessage(TranslatableMessages.MSG_REPAIR_STONE_TARGET_NOT_REPAIRABLE)
            return
        }

        val damage = targetItem.getData(DataComponentTypes.DAMAGE)
        val maxDamage = targetItem.getData(DataComponentTypes.MAX_DAMAGE)

        // 目标物品必须是可损坏的
        if (damage == null || maxDamage == null) {
            return
        }

        // 目标物品必须有损伤 (耐久度未满)
        if (damage <= 0) {
            player.sendMessage(TranslatableMessages.MSG_REPAIR_STONE_ALREADY_FULL)
            player.playSound(player, Sound.ENTITY_SHULKER_HURT, 1f, 1f)
            event.isCancelled = true
            return
        }

        // 计算修复量并应用
        val repairAmount = repairStoneData.computeRepairAmount(maxDamage)
        val newDamage = (damage - repairAmount).coerceAtLeast(0)
        targetItem.setData(DataComponentTypes.DAMAGE, newDamage)

        // 消耗修复石
        repairStoneItem.subtract()

        // 播放音效和发送消息
        player.playSound(player, Sound.BLOCK_ANVIL_USE, 1f, 1f)
        player.sendMessage(TranslatableMessages.MSG_REPAIR_STONE_SUCCESS)

        // 取消事件, 防止物品被交换到 cursor 上
        event.isCancelled = true

        // 延迟 1t 更新背包, 防止视觉上的物品消失
        runTaskLater(1) { player.updateInventory() }
    }
}