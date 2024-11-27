package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.util.unsafeNekooTagOrNull
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemXVanilla(
    identifier: String,
) : ItemXAbstract(ItemXFactoryVanilla.plugin, identifier) {
    companion object {
        const val DEFAULT_DISPLAY_NAME = "<white>UNKNOWN</white>"
    }

    override fun valid(): Boolean {
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
        // 在 wakame 这个体系下, 物品没有萌芽标签则认为是原版物品.
        // 这样的判断比 ItemStack#hasItemMeta 更能准确反应现实情况,
        // 例如: 一个拥有附魔的原版(套皮)物品也可以被正确的认为是原版物品.
        return itemStack.unsafeNekooTagOrNull == null &&
                itemStack.type == Material.matchMaterial(identifier, false)
    }

    override fun displayName(): String {
        val material = Material.matchMaterial(identifier, false) ?: return DEFAULT_DISPLAY_NAME
        return "<tr:${material.translationKey()}>"
    }
}

object ItemXFactoryVanilla : ItemXFactory {
    override val plugin: String = "minecraft"

    override val loaded: Boolean = true

    override fun create(itemStack: ItemStack): ItemXVanilla {
        return ItemXVanilla(itemStack.type.key.value())
    }

    override fun create(plugin: String, identifier: String): ItemXVanilla? {
        if (plugin != this.plugin)
            return null
        return ItemXVanilla(identifier)
    }
}