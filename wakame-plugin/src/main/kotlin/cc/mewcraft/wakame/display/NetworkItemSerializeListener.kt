package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStackFactory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.purpurmc.purpur.event.packet.NetworkItemSerializeEvent

internal class NetworkItemSerializeListener(
    private val renderer: ItemRenderer<PlayNekoStack>,
) : Listener {

    // Side notes: this event runs on the Netty Thread,
    // but the Server Thread will wait before next tick.
    @EventHandler
    private fun onSerializeItem(e: NetworkItemSerializeEvent) {
        // ensure it is a NekoItem realization & PlayNekoStack
        val nekoStack = PlayNekoStackFactory.maybe(e.itemStack) ?: return
        // render it in-place
        renderer.render(nekoStack)
        // set it to the rendered item
        e.setItemStack(nekoStack.itemStack)
    }

}