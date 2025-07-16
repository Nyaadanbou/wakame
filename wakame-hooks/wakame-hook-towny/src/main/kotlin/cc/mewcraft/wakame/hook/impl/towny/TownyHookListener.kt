package cc.mewcraft.wakame.hook.impl.towny

import com.palmergames.bukkit.towny.`object`.WorldCoord
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent

object TownyHookListener : Listener {
    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        val chunk = event.chunk
        val world = chunk.world
        val townBlock = TownyHook.TOWNY.getTownBlock(WorldCoord(world.name, chunk.x, chunk.z)) ?: return
        val town = townBlock.townOrNull ?: return

        // 加载整个城镇的区块
        for (tb in town.townBlocks) {
            val tbWorld = tb.world.bukkitWorld ?: continue
            val loadedChunk = tbWorld.getChunkAt(tb.x, tb.z)
            if (!loadedChunk.isLoaded) {
                tb.worldCoord.loadChunks()
                loadedChunk.isForceLoaded = true
            }
        }

    }
}