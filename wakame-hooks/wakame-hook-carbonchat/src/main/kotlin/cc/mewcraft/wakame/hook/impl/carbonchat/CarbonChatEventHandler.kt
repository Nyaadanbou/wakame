package cc.mewcraft.wakame.hook.impl.carbonchat

import cc.mewcraft.wakame.item.network.ItemStackRenderer
import cc.mewcraft.wakame.util.KoishKeys
import net.draycia.carbon.api.CarbonChatProvider
import net.draycia.carbon.api.event.events.CarbonChatEvent
import net.draycia.carbon.api.users.CarbonPlayer
import net.draycia.carbon.api.util.KeyedRenderer
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

object CarbonChatEventHandler {

    private val KOISH_RENDERER_KEY = KoishKeys.ofKoish("default")

    private val KOISH_RENDERER = KeyedRenderer.keyedRenderer(KOISH_RENDERER_KEY, ::render)

    private fun render(sender: CarbonPlayer, recipient: Audience, message: Component, originalMessage: Component): Component {
        val player = Bukkit.getPlayer(sender.uuid())
        val changed = ItemStackRenderer.renderShowItem(player, message)
        return changed
    }

    fun subscribe() {
        CarbonChatProvider.carbonChat().eventHandler().subscribe(CarbonChatEvent::class.java) { event ->
            event.renderers() += KOISH_RENDERER
        }
    }
}