package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.item.KoishStackImplementations
import cc.mewcraft.wakame.util.item.toNMS
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemXVanilla(
    identifier: String,
) : ItemXAbstract(ItemXFactoryVanilla.plugin, identifier) {
    companion object {
        const val DEFAULT_DISPLAY_NAME = "<red>Unknown Vanilla Item Type"
    }

    override fun valid(): Boolean {
        return true
    }

    override fun createItemStack(amount: Int, player: Player?): ItemStack? {
        return Material.matchMaterial(identifier, false)?.let { mat -> ItemStack(mat, amount) }
    }

    override fun matches(itemStack: ItemStack): Boolean {
        // 在 wakame 这个体系下, 物品没有萌芽标签则认为是原版物品.
        // 这样的判断比 ItemStack#hasItemMeta 更能准确反应现实情况,
        // 例如: 一个拥有附魔的原版(套皮)物品也可以被正确的认为是原版物品.
        return KoishStackImplementations.getNbt(itemStack.toNMS()) == null &&
                itemStack.type == Material.matchMaterial(identifier, false)
    }

    override fun displayName(): String {
        return Material.matchMaterial(identifier, false)
            ?.let { material -> "<tr:${material.translationKey()}>" }
            ?: DEFAULT_DISPLAY_NAME
    }
}

object ItemXFactoryVanilla : ItemXFactory {
    override val plugin: String = "minecraft"
    override val loaded: Boolean = true

    override fun create(itemStack: ItemStack): ItemXVanilla {
        return ItemXVanilla(itemStack.type.key.value())
    }

    override fun create(plugin: String, identifier: String): ItemXVanilla? {
        return if (plugin == this.plugin)
            ItemXVanilla(identifier)
        else null
    }
}