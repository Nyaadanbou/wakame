package core

import cc.mewcraft.wakame.core.ItemXAbstract
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemXMock(
    plugin: String,
    identifier: String,
) : ItemXAbstract(plugin, identifier) {
    constructor(uid: String) : this(
        uid.substringBefore(':'), uid.substringAfter(':')
    )

    override fun isValid(): Boolean {
        throw NotImplementedError()
    }

    override fun createItemStack(): ItemStack? {
        throw NotImplementedError()
    }

    override fun createItemStack(player: Player): ItemStack? {
        throw NotImplementedError()
    }

    override fun matches(itemStack: ItemStack): Boolean {
        throw NotImplementedError()
    }

    override fun renderName(): String {
        throw NotImplementedError()
    }
}