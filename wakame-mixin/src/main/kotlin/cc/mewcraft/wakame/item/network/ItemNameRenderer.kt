package cc.mewcraft.wakame.item.network

import cc.mewcraft.wakame.ability.component.OnceOffItemName
import cc.mewcraft.wakame.ecs.bridge.canKoishify
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.util.adventure.removeItalic
import cc.mewcraft.wakame.util.adventure.toNMSComponent
import cc.mewcraft.wakame.util.item.toNMS
import cc.mewcraft.wakame.util.serverPlayer
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponents
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.entity.player.Inventory
import org.bukkit.entity.Player

/**
 * 本 object 用于在客户端侧制造一个虚拟的物品命名效果.
 * 由于是虚拟的, 所以不会在服务端侧实际改变物品的名称.
 */
object ItemNameRenderer {

    /**
     * 制造一个虚拟的物品命名效果. 虚拟的效果仅在客户端侧生效, 不会影响服务端侧的数据.
     *
     * 具体是将玩家 [receiver] 背包中位于 [index] 上的物品堆叠的 `minecraft:custom_name` 修改为 [name].
     */
    fun send(index: Int, name: Component, receiver: Player) {
        val serverPlayer = receiver.serverPlayer
        val heldItemSlot = getSlot(index)
        val item = receiver.inventory.getItem(index)?.toNMS()?.copy() ?: return
        if (item.isEmpty) {
            return
        }
        item.set(DataComponents.CUSTOM_NAME, name.toNMSComponent())
        val packet = ClientboundContainerSetSlotPacket(serverPlayer.inventoryMenu.containerId, serverPlayer.inventoryMenu.incrementStateId(), heldItemSlot, item)
        serverPlayer.connection.send(packet)
    }

    /**
     * 将服务端侧的背包内容同步到客户端. 这将抹除掉一切虚拟的物品命名效果.
     */
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
            index + 5 // offhand
        } else if (index > 35) {
            8 - (index - 36)
        } else {
            index
        }
        return result
    }
}

fun Player.sendItemNameChangeInMainHand(component: Component, duration: Long) {
    if (!canKoishify())
        return
    koishify() += OnceOffItemName(
        inventory.heldItemSlot,
        component.removeItalic,
        duration
    )
}