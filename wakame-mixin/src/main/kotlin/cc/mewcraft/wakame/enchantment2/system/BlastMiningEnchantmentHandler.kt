package cc.mewcraft.wakame.enchantment2.system

import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayer
import cc.mewcraft.wakame.ecs.system.ListenableIteratingSystem
import cc.mewcraft.wakame.enchantment2.component.BlastMining
import com.github.quillraven.fleks.Entity
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityExplodeEvent

/**
 * @see cc.mewcraft.wakame.enchantment2.effect.EnchantmentBlastMiningEffect
 */
object BlastMiningEnchantmentHandler : ListenableIteratingSystem(
    family = EWorld.family { all(BukkitObject, BukkitPlayer, BlastMining) }
) {

    override fun onTickEntity(entity: Entity) {
        // 无操作
    }

    @JvmStatic
    val runningBlock: ThreadLocal<Block> = ThreadLocal()

    @EventHandler
    fun on(event: BlockBreakEvent) {
        val player = event.player
        val playerEntity = player.koishify().unwrap()
        val blastMining = playerEntity.getOrNull(BlastMining) ?: return
        if (player.isSneaking) return // 潜行时不触发该魔咒效果
        val block = event.block
        if (block == this.runningBlock.get()) return // 禁止 BlastMining 破坏的方块再次触发 BlastMining
        if (block == VeinminerEnchantmentHandler.runningBlock.get()) return // 禁止 Veinminer 破坏的方块触发 BlastMining

        block.world.createExplosion(/* source = */ player, /* loc = */ block.location, /* power = */ blastMining.explosionPower, /* setFire = */ false, /* breakBlocks = */ true, /* excludeSourceFromDamage = */ true)
        // ... 这里将接着执行 EntityExplodeEvent 的逻辑
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun on(event: EntityExplodeEvent) {
        val player = event.entity as? Player ?: return
        val playerEntity = player.koishify().unwrap()
        val blastMining = playerEntity.getOrNull(BlastMining) ?: return

        // 不让爆炸真的炸毁方块, 而是强制让玩家破坏将要炸毁的方块.
        // 这样可以触发 BlockBreakEvent 以“兼容”其他系统如领地.
        val blockList = event.blockList()
        for (block in blockList) {
            if (block.location == event.location || !blastMining.isHardEnough(block)) continue
            this.runningBlock.set(block)
            player.breakBlock(block)
            // ... 这里将接着执行 BlockBreakEvent 的逻辑
            this.runningBlock.remove()
        }
        blockList.clear()
    }

}