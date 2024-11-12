package cc.mewcraft.wakame.core

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemXVanilla(
    identifier: String,
) : ItemXAbstract(ItemXFactoryVanilla.plugin, identifier) {
    companion object {
        const val DEFAULT_DISPLAY_NAME = "<white>UNKNOWN</white>"
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun createItemStack(): ItemStack? {
        val material = Material.matchMaterial(identifier, false) ?: return null
        return ItemStack(material)
    }

    override fun createItemStack(player: Player): ItemStack? {
        return createItemStack()
    }

    override fun matches(itemStack: ItemStack): Boolean {
        // TODO 使用是否存在Custom Data判断
        return !itemStack.hasItemMeta() && itemStack.type == Material.matchMaterial(identifier, false)
    }

    override fun displayName(): String {
        val material = Material.matchMaterial(identifier, false) ?: return DEFAULT_DISPLAY_NAME
        return "<tr:${material.translationKey()}>"
    }
}

object ItemXFactoryVanilla : ItemXFactory {
    override val plugin: String = "minecraft"

    override val isValid: Boolean = true

    override fun create(itemStack: ItemStack): ItemXVanilla {
        return ItemXVanilla(itemStack.type.key.value())
    }

    override fun create(plugin: String, identifier: String): ItemXVanilla? {
        if (plugin != this.plugin)
            return null
        return ItemXVanilla(identifier)
    }
}