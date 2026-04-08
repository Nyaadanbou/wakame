package cc.mewcraft.wakame.enchantment.system

import cc.mewcraft.wakame.enchantment.component.VeinminerChild
import cc.mewcraft.wakame.enchantment.effect.EnchantmentVeinminerEffect
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.runTaskTimerSelfAware
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.scheduler.BukkitTask

/**
 * @see cc.mewcraft.wakame.enchantment.effect.EnchantmentVeinminerEffect
 */
object EnchantmentVeinminerSystem : Listener {

    private val validAdjacentFaces: Array<BlockFace> = arrayOf(
        BlockFace.WEST,
        BlockFace.EAST,
        BlockFace.NORTH,
        BlockFace.SOUTH,
        BlockFace.UP,
        BlockFace.DOWN,
    )

    @JvmStatic
    val runningBlock: ThreadLocal<Block> = ThreadLocal()

    // 挖掘了特定方块后触发连锁效果
    @EventHandler
    fun on(event: BlockBreakEvent) {
        val block = event.block
        if (block == runningBlock.get()) return // 禁止 Veinminer 破坏的方块再次触发 Veinminer
        if (block == EnchantmentBlastMiningSystem.runningBlock.get()) return // 禁止 BlastMining 破坏的方块触发 Veinminer
        val player = event.player
        if (player.isSneaking) return // 潜行时不触发该魔咒效果
        val metadata = player.metadata()
        val veinminer = metadata.getOrNull(EnchantmentVeinminerEffect.DATA_KEY) ?: return
        val allowedBlockTypes = veinminer.allowedBlockTypes
        if (block.type !in allowedBlockTypes) return

        val child = VeinminerChild(player, veinminer, block)

        // 起始方块已被玩家自己破坏, 只需将其邻居加入队列用于 BFS 扩展
        // 起始方块本身不应再被 breakBlock, 否则会导致声音播放两次
        child.queue.removeFirst()
        for (face in validAdjacentFaces) {
            val neighbor = block.getRelative(face)
            if (child.sameType(neighbor) && child.visited.add(neighbor)) {
                child.queue.addLast(neighbor)
            }
        }
        if (child.queue.isEmpty()) return // 没有相邻的同类方块, 无需启动连锁效果

        runTaskTimerSelfAware(0, child.parent.period) { task -> runChild(task, child) }
    }

    private fun runChild(task: BukkitTask, child: VeinminerChild) {
        val player = child.player
        if (player.isConnected.not() || player.metadata().has(EnchantmentVeinminerEffect.DATA_KEY).not()) {
            task.cancel()
            return // 玩家离线或失去该魔咒, 停止连锁采矿效果
        }

        // BFS

        val queue = child.queue
        if (queue.isEmpty() || child.currentCount++ > child.maximumCount) {
            task.cancel()
            return // 整个矿脉已遍历完毕或达到遍历次数上限
        }

        val bukkitBlock = queue.removeFirst()

        runningBlock.set(bukkitBlock)
        player.breakBlock(bukkitBlock)
        // ... 这里将接着执行 BlockBreakEvent 的逻辑
        runningBlock.remove()

        val visited = child.visited
        for (face in validAdjacentFaces) {
            val neighbor = bukkitBlock.getRelative(face)
            if (child.sameType(neighbor) && visited.add(neighbor)) {
                queue.addLast(neighbor)
            }
        }
    }
}