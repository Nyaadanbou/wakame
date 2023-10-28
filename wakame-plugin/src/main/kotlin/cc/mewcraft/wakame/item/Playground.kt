package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.util.readNbt
import org.bukkit.inventory.ItemStack

class Playground {
    fun play(item: ItemStack) {
        item.readNbt()?.let {
            
        }
    }
}