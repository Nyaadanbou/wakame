package cc.mewcraft.wakame.event.bukkit

import net.minecraft.world.InteractionHand
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack

class PlayerItemRightClickEvent(
    player: Player,
    val item: ItemStack,
    hand: InteractionHand,
) : PlayerEvent(player) {

    init {
        player.sendMessage("PlayerItemRightClickEvent called, hand = $hand")
    }

    val hand = when (hand) {
        InteractionHand.MAIN_HAND -> Hand.MAIN_HAND
        InteractionHand.OFF_HAND -> Hand.OFF_HAND
    }

    enum class Hand {
        MAIN_HAND,
        OFF_HAND;
    }

    override fun getHandlers(): HandlerList {
        return HANDLER_LIST
    }

    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}