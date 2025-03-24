package cc.mewcraft.wakame.enchantment2.system

import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.BukkitObject
import cc.mewcraft.wakame.ecs.component.BukkitPlayerComponent
import cc.mewcraft.wakame.ecs.system.ListenableIteratingSystem
import cc.mewcraft.wakame.enchantment2.component.BlastMining
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityExplodeEvent

/**
 * @see cc.mewcraft.wakame.enchantment2.effect.EnchantmentBlastMiningEffect
 */
object EnchantmentBlastMiningSystem : ListenableIteratingSystem(
    family = World.family { all(BukkitObject, BukkitPlayerComponent, BlastMining) }
) {

    override fun onTickEntity(entity: Entity) {
        // 无操作
    }

    // 用来表示当前正在运行中的 BlastMining 的爆炸等级
    private var runningExplosion: ThreadLocal<Unit> = ThreadLocal()

    @EventHandler
    fun on(event: BlockBreakEvent) {
        if (this.runningExplosion.get() != null) return // EntityExplodeEvent 里的 Player#breakBlock 会再次触发该事件, 必须在这里终止递归

        val player = event.player
        val playerEntity = player.koishify().unwrap()
        val blastMining = playerEntity.getOrNull(BlastMining) ?: return
        if (player.isSneaking) return // 潜行时不触发该魔咒效果
        val block = event.block

        this.runningExplosion.set(Unit)
        block.world.createExplosion(/* source = */ player, /* loc = */ block.location, /* power = */ blastMining.explosionPower, /* setFire = */ false, /* breakBlocks = */ true, /* excludeSourceFromDamage = */ true)
        // ... 这里将接着运行 EntityExplodeEvent 的逻辑
        this.runningExplosion.remove()
    }

    // Process explosion event to mine blocks.
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun on(event: EntityExplodeEvent) {
        val player = event.entity as? Player ?: return
        val playerEntity = player.koishify().unwrap()
        val blastMining = playerEntity.getOrNull(BlastMining) ?: return

        event.yield = 1f // 使爆炸不损失任何方块

        val blockList = event.blockList()
        for (block in blockList) {
            if (block.location == event.location || !blastMining.isHardEnough(block)) {
                continue
            }

            player.breakBlock(block) // 这样破坏方块可以触发 BlockBreakEvent 以触发其他效果, 例如时运
        }
        blockList.clear()
    }

}