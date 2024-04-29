package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.PlayNekoStack
import org.bukkit.event.Listener

internal class NetworkItemSerializeListener(
    private val renderer: ItemRenderer<PlayNekoStack>,
) : Listener {

//    // Side notes: this event runs on the Netty Thread,
//    // but the Server Thread will wait before next tick.
//    @EventHandler
//    private fun onSerializeItem(e: NetworkItemSerializeEvent) {
//        // ensure it is a NekoItem realization & PlayNekoStack
//        val nekoStack = PlayNekoStackFactory.maybe(e.itemStack) ?: return
//        // render it in-place
//        renderer.render(nekoStack)
//        // set it to the rendered item
//        e.setItemStack(nekoStack.itemStack)
//    }

}