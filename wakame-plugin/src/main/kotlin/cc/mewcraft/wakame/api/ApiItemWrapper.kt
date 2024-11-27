package cc.mewcraft.wakame.api

import cc.mewcraft.wakame.api.block.NekoBlock
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.user.toUser
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import cc.mewcraft.wakame.api.item.NekoItem as INekoItem

internal class ApiItemWrapper(
    private val item: NekoItem,
) : INekoItem {
    override fun getId(): Key {
        return item.id
    }

    override fun getBlock(): NekoBlock? {
        TODO("Not yet implemented")
    }

    override fun getName(): Component {
        return item.name
    }

    override fun getPlainName(): String {
        return item.plainName
    }

    override fun createItemStack(amount: Int): ItemStack {
        val nekoStack = item.realize()
        val itemStack = nekoStack.wrapped.apply { this.amount = amount }
        return itemStack
    }

    override fun createItemStack(amount: Int, player: Player): ItemStack {
        val user = player.toUser()
        val nekoStack = item.realize(user)
        val itemStack = nekoStack.wrapped.apply { this.amount = amount }
        return itemStack
    }
}