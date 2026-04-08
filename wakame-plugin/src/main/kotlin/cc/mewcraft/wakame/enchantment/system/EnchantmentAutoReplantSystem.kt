package cc.mewcraft.wakame.enchantment.system

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.enchantment.component.AutoReplant
import cc.mewcraft.wakame.enchantment.effect.EnchantmentAutoReplantEffect
import cc.mewcraft.wakame.item.extension.damageItem
import cc.mewcraft.wakame.util.metadata.metadata
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * 自动种植魔咒的逻辑实现.
 *
 * 当玩家持有带有自动种植魔咒的物品右键点击已成熟的农作物时:
 * 1. 破坏农作物, 使其掉落物品
 * 2. 从玩家背包中查找并消耗一颗对应的种子
 * 3. 将农作物方块重新设为初始生长状态 (age=0)
 *
 * @see EnchantmentAutoReplantEffect
 */
object EnchantmentAutoReplantSystem : Listener {

    @EventHandler(ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return // 只处理主手, 避免触发两次

        val player = event.player
        val metadata = player.metadata()
        val autoReplant = metadata.getOrNull(EnchantmentAutoReplantEffect.DATA_KEY) ?: return

        val block = event.clickedBlock ?: return
        val blockData = block.blockData

        // 检查方块是否为可生长的农作物
        if (blockData !is Ageable) return

        // 检查农作物是否已成熟
        if (blockData.age < blockData.maximumAge) return

        // 检查该农作物是否在配置的映射中, 并尝试补种中心方块
        if (!replant(player, autoReplant, block)) return

        // 阻止事件继续传播, 避免触发其他右键交互
        event.isCancelled = true

        // 对范围内所有已成熟的作物执行相同的补种逻辑
        for (affectedBlock in autoReplant.getAffectedCrops(block)) {
            replant(player, autoReplant, affectedBlock)
        }
    }

    /**
     * 对单个已成熟的作物方块执行破坏 + 消耗种子 + 补种.
     *
     * @return 如果成功补种则返回 `true`, 背包中没有对应种子则返回 `false`
     */
    private fun replant(player: Player, autoReplant: AutoReplant, block: Block): Boolean {
        val seedMaterial = autoReplant.getSeed(block.type) ?: return false
        val inventory = player.inventory
        val seedSlot = inventory.contents.indexOfFirst { it != null && it.type == seedMaterial }
        if (seedSlot == -1) {
            player.sendActionBar(TranslatableMessages.MSG_ERR_ENCHANTMENT_AUTO_REPLANT_NO_SEEDS)
            return false
        }

        val cropType = block.type
        if (!player.breakBlock(block)) return false

        val seedStack = inventory.getItem(seedSlot) ?: return false
        seedStack.subtract()

        block.setType(cropType, true)
        val newBlockData = block.blockData
        if (newBlockData is Ageable) {
            newBlockData.age = 0
            block.setBlockData(newBlockData, true)
        }

        player.damageItem(EquipmentSlot.HAND, 1)
        return true
    }
}
