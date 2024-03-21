package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.NekoStackFactory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.purpurmc.purpur.event.packet.NetworkItemSerializeEvent

internal class ItemRendererListener(
    private val renderer: ItemRenderer,
) : Listener {

    @EventHandler
    private fun onSerializeItem(e: NetworkItemSerializeEvent) { // this event runs on Netty thread
        val wrap = NekoStackFactory.wrap(e.itemStack) // the wrapped item is already a copy
        if (wrap.isNotNeko)
            return

        renderer.render(wrap) // render it in-place
        e.setItemStack(wrap.handle)
    }

}