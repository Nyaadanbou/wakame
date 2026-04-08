package cc.mewcraft.wakame.bridge.item

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface ServerItemRef {
    fun createStack(amount: Int, player: Player?): ItemStack
}