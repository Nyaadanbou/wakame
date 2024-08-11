package cc.mewcraft.wakame.core

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemXVanilla(
    identifier: String,
) : ItemXAbstract(ItemXFactoryVanilla.plugin, identifier) {

    override fun createItemStack(): ItemStack? {
        val material = Material.matchMaterial(identifier, false) ?: return null
        return ItemStack(material)
    }

    override fun createItemStack(player: Player): ItemStack? {
        return createItemStack()
    }

    override fun matches(itemStack: ItemStack): Boolean {
        return !itemStack.hasItemMeta() && itemStack.type == Material.matchMaterial(identifier, false)
    }
}

object ItemXFactoryVanilla : ItemXFactory {
    override val plugin: String = "minecraft"

    override val isValid: Boolean = true

    override fun byItemStack(itemStack: ItemStack): ItemXVanilla {
        return ItemXVanilla(itemStack.type.key.value())
    }

    override fun byUid(plugin: String, itemId: String): ItemXVanilla? {
        if (plugin != this.plugin)
            return null
        return ItemXVanilla(itemId)
    }
}