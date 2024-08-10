package cc.mewcraft.wakame.core

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class VanillaItemX(
    override val itemId: String,
) : ItemX {
    override val plugin: String = "minecraft"
    override fun createItemStack(): ItemStack? {
        val material = Material.matchMaterial(itemId, false) ?: return null
        return ItemStack(material)
    }

    override fun createItemStack(player: Player): ItemStack? {
        return createItemStack()
    }

    override fun matches(itemStack: ItemStack): Boolean {
        return !itemStack.hasItemMeta() && itemStack.type == Material.matchMaterial(itemId, false)
    }

}

class VanillaItemXBuilder : ItemXBuilder<VanillaItemX> {
    override val available: Boolean = true
    override fun byItemStack(itemStack: ItemStack): VanillaItemX {
        return VanillaItemX(itemStack.type.key.value())
    }

    override fun byReference(plugin: String, itemId: String): VanillaItemX? {
        if (plugin != "minecraft") return null
        return VanillaItemX(itemId)
    }

    //TODO 注册
}