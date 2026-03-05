package cc.mewcraft.wakame.enchantment.system

import cc.mewcraft.wakame.enchantment.component.RangeMiningChild
import cc.mewcraft.wakame.enchantment.effect.EnchantmentRangeMiningEffect
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.runTaskTimer
import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageAbortEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 范围挖掘魔咒的逻辑实现.
 *
 * 该系统通过监听以下事件来实现范围挖掘:
 * - [BlockDamageEvent]: 玩家开始挖掘时, 记录挖掘的方块面 ([BlockFace])
 * - [BlockBreakProgressUpdateEvent]: 挖掘进度更新时, 向客户端发送范围内所有方块的假破坏动画
 * - [BlockBreakEvent]: 方块被破坏时, 启动定时任务逐个破坏范围内的方块
 * - [BlockDamageAbortEvent]: 玩家停止挖掘时, 取消范围内所有方块的破坏动画
 *
 * @see EnchantmentRangeMiningEffect
 */
object TickRangeMiningEnchantment : Listener {

    /**
     * 用于防止范围挖掘破坏的方块再次触发范围挖掘的递归循环.
     */
    @JvmStatic
    val runningBlock: ThreadLocal<Block> = ThreadLocal()

    /**
     * 记录每个玩家当前正在挖掘的方块面.
     *
     * 当 [BlockDamageEvent] 触发时写入, 当 [BlockDamageAbortEvent] 或 [BlockBreakEvent] 触发时移除.
     */
    private val playerMiningFace: ConcurrentHashMap<UUID, BlockFace> = ConcurrentHashMap()

    /**
     * 当玩家开始挖掘一个方块时, 记录挖掘的方块面.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockDamageEvent) {
        val player = event.player
        val metadata = player.metadata()
        metadata.getOrNull(EnchantmentRangeMiningEffect.DATA_KEY) ?: return
        if (player.isSneaking) return // 潜行时不触发该魔咒效果
        playerMiningFace[player.uniqueId] = event.blockFace
    }

    /**
     * 当玩家挖掘方块时, 方块破坏进度不断更新, 每次更新时向客户端发送范围内方块的假破坏动画.
     */
    @EventHandler
    fun on(event: BlockBreakProgressUpdateEvent) {
        val player = event.entity as? Player ?: return
        val metadata = player.metadata()
        val rangeMining = metadata.getOrNull(EnchantmentRangeMiningEffect.DATA_KEY) ?: return
        if (player.isSneaking) return // 潜行时不触发该魔咒效果

        val face = playerMiningFace[player.uniqueId] ?: return
        val block = event.block
        val progress = event.progress
        val centerType = block.type

        // 获取范围内所有受影响的方块, 并发送假的破坏进度
        val affectedBlocks = rangeMining.getAffectedBlocks(block, face)
        for (affectedBlock in affectedBlocks) {
            if (rangeMining.shouldAffect(affectedBlock, centerType)) {
                player.sendBlockDamage(affectedBlock.location, progress, affectedBlock.hashCode())
            }
        }
    }

    /**
     * 当方块确实被玩家破坏时, 启动定时任务逐个破坏范围内的方块.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {
        val block = event.block
        if (block == runningBlock.get()) return // 防止递归: 被范围挖掘破坏的方块不再触发范围挖掘
        if (block == TickBlastMiningEnchantment.runningBlock.get()) return // 防止与其他挖掘魔咒冲突
        if (block == TickVeinminerEnchantment.runningBlock.get()) return // 防止与其他挖掘魔咒冲突

        val player = event.player
        val metadata = player.metadata()
        val rangeMining = metadata.getOrNull(EnchantmentRangeMiningEffect.DATA_KEY) ?: return
        if (player.isSneaking) return // 潜行时不触发该魔咒效果

        val face = playerMiningFace.remove(player.uniqueId) ?: return

        // 收集范围内所有受影响的方块
        val centerType = block.type
        val affectedBlocks = rangeMining.getAffectedBlocks(block, face)
        val queue = ArrayDeque<Block>(affectedBlocks.size)
        for (affectedBlock in affectedBlocks) {
            if (rangeMining.shouldAffect(affectedBlock, centerType)) {
                // 清除假的破坏动画
                player.sendBlockDamage(affectedBlock.location, 0f, affectedBlock.hashCode())
                queue.addLast(affectedBlock)
            }
        }
        if (queue.isEmpty()) return

        val child = RangeMiningChild(player, rangeMining, centerType, queue)
        runTaskTimer(0, rangeMining.period) { task -> runChild(task, child) }
    }

    private fun runChild(task: BukkitTask, child: RangeMiningChild) {
        val player = child.player
        if (!player.isConnected || !player.metadata().has(EnchantmentRangeMiningEffect.DATA_KEY)) {
            task.cancel()
            return // 玩家离线或失去该魔咒, 停止范围挖掘
        }

        val queue = child.queue
        if (queue.isEmpty()) {
            task.cancel()
            return // 所有方块已破坏完毕
        }

        val bukkitBlock = queue.removeFirst()

        // 方块可能在等待期间已经被其他原因破坏
        if (bukkitBlock.type.isAir) return

        runningBlock.set(bukkitBlock)
        player.breakBlock(bukkitBlock)
        runningBlock.remove()
    }

    /**
     * 当玩家停止挖掘时, 取消范围内所有方块的破坏动画.
     */
    @EventHandler
    fun on(event: BlockDamageAbortEvent) {
        val player = event.player
        val metadata = player.metadata()
        val rangeMining = metadata.getOrNull(EnchantmentRangeMiningEffect.DATA_KEY) ?: return

        val face = playerMiningFace.remove(player.uniqueId) ?: return
        val block = event.block

        // 发送 progress=0 的假破坏动画以清除客户端上的破坏效果
        val affectedBlocks = rangeMining.getAffectedBlocks(block, face)
        for (affectedBlock in affectedBlocks) {
            if (!affectedBlock.type.isAir) {
                player.sendBlockDamage(affectedBlock.location, 0f, affectedBlock.hashCode())
            }
        }
    }
}