package cc.mewcraft.wakame.item2.network

import cc.mewcraft.wakame.ability2.component.OnceOffItemName
import cc.mewcraft.wakame.ecs.bridge.canKoishify
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.util.item.toNMS
import cc.mewcraft.wakame.util.serverPlayer
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponents
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.entity.player.Inventory
import org.bukkit.entity.Player

object ItemNameRenderer {
    fun send(index: Int, component: Component, player: Player) {
        val serverPlayer = player.serverPlayer
        val heldItemSlot = getSlot(index)
        val item = player.inventory.getItem(index)?.toNMS()?.copy() ?: return
        if (item.isEmpty) {
            return
        }
        item.set(DataComponents.CUSTOM_NAME, PaperAdventure.asVanilla(component))
        val packet = ClientboundContainerSetSlotPacket(serverPlayer.inventoryMenu.containerId, serverPlayer.inventoryMenu.incrementStateId(), heldItemSlot, item)
        serverPlayer.connection.send(packet)
    }

    fun resync(player: Player) {
        player.serverPlayer.containerMenu.sendAllDataToRemote()
    }

    /**
     * Source: [org.bukkit.craftbukkit.inventory.CraftInventoryPlayer.setItem]
     */
    private fun getSlot(index: Int): Int {
        val result = if (index < Inventory.getSelectionSize()) {
            index + 36
        } else if (index > 39) {
            index + 5 // Off hand
        } else if (index > 35) {
            8 - (index - 36)
        } else {
            index
        }

        return result
    }
}

fun Player.sendItemName(index: Int, component: Component, duration: Long) {
    if (!canKoishify())
        return
    koishify() += OnceOffItemName(index, component, duration)
}