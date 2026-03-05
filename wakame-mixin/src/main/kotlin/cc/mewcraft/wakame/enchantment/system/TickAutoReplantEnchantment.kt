package cc.mewcraft.wakame.enchantment.system

import cc.mewcraft.wakame.enchantment.effect.EnchantmentAutoReplantEffect
import cc.mewcraft.wakame.util.metadata.metadata
import org.bukkit.block.data.Ageable
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
object TickAutoReplantEnchantment : Listener {

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

        // 检查该农作物是否在配置的映射中
        val seedMaterial = autoReplant.getSeed(block.type) ?: return

        // 从玩家背包中查找第一个对应的种子
        val inventory = player.inventory
        val seedSlot = inventory.contents.indexOfFirst { it != null && it.type == seedMaterial }
        if (seedSlot == -1) return // 背包中没有对应的种子

        // 记住农作物类型, 因为 breakBlock 之后方块就变了
        val cropType = block.type

        // 破坏农作物, 掉落物品
        if (!player.breakBlock(block)) return

        // 消耗一颗种子
        val seedStack = inventory.getItem(seedSlot) ?: return
        seedStack.subtract()

        // 补种: 设置方块为农作物的初始状态
        block.setType(cropType, true)
        val newBlockData = block.blockData
        if (newBlockData is Ageable) {
            newBlockData.age = 0
            block.setBlockData(newBlockData, true)
        }

        // 阻止事件继续传播, 避免触发其他右键交互
        event.isCancelled = true
    }
}
