package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.NekoItemStackFactory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.purpurmc.purpur.event.packet.NetworkItemSerializeEvent

internal class ItemRendererListener(
    private val renderer: ItemRenderer,
) : Listener {

    @EventHandler
    private fun onSerializeItem(e: NetworkItemSerializeEvent) {
        val wrap = NekoItemStackFactory.wrap(e.itemStack) // the wrapped ItemStack is a copy
        if (wrap.isNotNeko) {
            return
        }

        renderer.render(wrap) // render it in-place
        val modified = wrap.handle
        e.setItemStack(modified)
    }

}