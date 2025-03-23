package cc.mewcraft.wakame.enchantment2.system

import cc.mewcraft.wakame.LOGGER
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
    private var explodeLevel: ThreadLocal<Int> = ThreadLocal()

    @EventHandler
    fun on(event: BlockBreakEvent) {
        val player = event.player
        val playerEntity = player.koishify().unwrap()
        val blastMining = playerEntity.getOrNull(BlastMining) ?: return
        val block = event.block

        // 标记 - 开始执行爆炸逻辑
        this.explodeLevel.set(blastMining.explodeLevel)

        block.world.createExplosion(/* source = */ player, /* loc = */ block.location, /* power = */ explodeLevel.get().toFloat(), /* setFire = */ false, /* breakBlocks = */ true, /* excludeSourceFromDamage = */ true)
        // 这里会接着执行 EntityExplodeEvent 的代码

        // 标记 - 结束执行爆炸逻辑
        this.explodeLevel.remove()

        LOGGER.info("BlockBreakEvent passed to BlastMiningSystem")
    }

    // Process explosion event to mine blocks.
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun on(event: EntityExplodeEvent) {
        if (event.entity !is Player) return
        if (this.explodeLevel.get() == null) return // 没有正在执行的爆炸逻辑

        event.yield = 1f // 使爆炸不损失任何方块

        LOGGER.info("EntityExplodeEvent passed to BlastMiningSystem")
    }

}