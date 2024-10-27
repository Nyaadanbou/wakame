package cc.mewcraft.wakame.chat

import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.StandardContext
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.packet.customData
import cc.mewcraft.wakame.packet.tryNekoStack
import cc.mewcraft.wakame.util.toItemStack
import com.github.retrooper.packetevents.protocol.item.ItemStack
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import net.draycia.carbon.api.CarbonChat
import net.draycia.carbon.api.CarbonChatProvider
import net.draycia.carbon.api.event.events.CarbonChatEvent
import net.draycia.carbon.api.event.events.CarbonPrivateChatEvent
import net.draycia.carbon.api.users.CarbonPlayer
import net.draycia.carbon.api.util.KeyedRenderer
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.util.function.UnaryOperator

internal class ItemChatRenderer : Initializable, Listener {
    override fun onPreWorld() {
        val carbon: CarbonChat = CarbonChatProvider.carbonChat()
        val eventHandler = carbon.eventHandler()
        eventHandler.subscribe(CarbonChatEvent::class.java) { e ->
            val renderers = e.renderers()
            renderers.add(KeyedItemRenderer)
        }
        eventHandler.subscribe(CarbonPrivateChatEvent::class.java) { e ->
            val message = e.message()
            val new = KeyedItemRenderer.render(e.sender(), e.recipient(), message, message)
            e.message(new)
        }
    }
}

private object KeyedItemRenderer : KeyedRenderer, KoinComponent {
    private val logger: Logger by inject()

    override fun key(): Key {
        return Key.key("wakame:item_renderer")
    }

    override fun render(sender: CarbonPlayer, recipient: Audience, message: Component, originalMessage: Component): Component {
        return modifyComponent(message)
    }

    private fun modifyComponent(component0: Component): Component {
        var component = component0
        if (component is TranslatableComponent) {
            val newArgs: List<Component> = component.arguments()
                .map { arg -> modifyComponent(arg.asComponent()) }
            component = component.arguments(newArgs)
        }

        val newChildren = component.children()
            .map { child -> modifyComponent(child) }
            .toList()
        component = component.children(newChildren)

        val hoverEvent = component.style().hoverEvent()
        val showItem = hoverEvent?.value()
        if (hoverEvent != null && showItem is HoverEvent.ShowItem) {
            val itemStack = showItem.toItemStack()
            val packetItemStack = SpigotConversionUtil.fromBukkitItemStack(itemStack)
            val updated = packetItemStack.modify()
            if (!updated) {
                return component
            }

            val newItem = SpigotConversionUtil.toBukkitItemStack(packetItemStack)
            val newHover = newItem.asHoverEvent(UnaryOperator.identity())
            component = component.style(component.style().hoverEvent(newHover))
        }

        return component
    }

    /**
     * @return 如果物品发生了变化则返回 `true`, 否则返回 `false`
     */
    private fun ItemStack.modify(): Boolean {
        var changed = false

        // 移除任意物品的 PDC
        changed = changed || customData?.removeTag("PublicBukkitValues") != null

        val nekoStack = tryNekoStack
        if (nekoStack != null) {
            try {
                ItemRenderers.STANDARD.render(nekoStack, StandardContext)
                changed = true
            } catch (e: Throwable) {
                logger.error("An error occurred while rendering NekoStack: $this", e)
            }
        }

        return changed
    }
}