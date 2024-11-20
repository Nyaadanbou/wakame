package cc.mewcraft.wakame.compatibility.chestshort

import de.jeff_media.chestsort.api.ChestSortEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChestSortListener : Listener {
    @EventHandler(ignoreCancelled = true)
    fun on(event: ChestSortEvent) {
        // TODO 检查 slot 是不是饰品栏,
        //  如果是, 检查里面有没有饰品
        //  如果有, 则不整理该 slot
    }
}