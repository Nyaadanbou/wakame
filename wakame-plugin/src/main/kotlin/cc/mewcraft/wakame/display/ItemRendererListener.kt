package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.NekoStackFactory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.purpurmc.purpur.event.packet.NetworkItemSerializeEvent

internal class ItemRendererListener(
    private val renderer: ItemRenderer,
) : Listener {

    // Side notes: this event runs on the Netty Thread,
    // but the Server Thread will wait before next tick.
    @EventHandler
    private fun onSerializeItem(e: NetworkItemSerializeEvent) {
        val wrap = NekoStackFactory.by(e.itemStack) ?: return
        // render it in-place
        renderer.render(wrap)
        // set it to the new item
        e.setItemStack(wrap.handle)
    }

}