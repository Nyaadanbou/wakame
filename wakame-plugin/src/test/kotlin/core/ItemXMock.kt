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

    override fun valid(): Boolean {
        throw NotImplementedError()
    }

    override fun createItemStack(amount: Int, player: Player?): ItemStack? {
        throw NotImplementedError()
    }

    override fun matches(itemStack: ItemStack): Boolean {
        throw NotImplementedError()
    }

    override fun displayName(): String {
        throw NotImplementedError()
    }
}