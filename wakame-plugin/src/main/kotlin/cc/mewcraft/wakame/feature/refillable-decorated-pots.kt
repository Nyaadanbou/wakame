package cc.mewcraft.wakame.feature

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

// TODO 可重复获取的陶罐, 有可能实现吗?
class RefillableDecoratedPots : Listener {

    @EventHandler(ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {

    }
}