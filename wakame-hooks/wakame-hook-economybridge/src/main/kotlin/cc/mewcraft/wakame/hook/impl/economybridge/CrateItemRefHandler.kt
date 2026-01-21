package cc.mewcraft.wakame.hook.impl.economybridge

import cc.mewcraft.wakame.item.ItemRefHandler
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import su.nightexpress.economybridge.api.item.ItemHandler

// 让 Koish 能够识别 ExcellentCrates 的物品 (实体盲盒钥匙)
class CrateItemRefHandler(
    private val handler: ItemHandler,
) : ItemRefHandler<ItemStack> {

    companion object {
        const val NAMESPACE = "excellentcrates"
    }

    override val systemName: String = "ExcellentCrates"

    override fun accepts(id: Identifier): Boolean {
        return id.namespace() == NAMESPACE && handler.isValidId(id.value())
    }

    override fun getId(stack: ItemStack): Identifier? {
        val id = handler.getItemId(stack) ?: return null
        return Identifiers.tryParse(NAMESPACE, id)
    }

    override fun getName(id: Identifier): Component? {
        return Component.text(id.asString())
    }

    override fun getInternalType(id: Identifier): ItemStack? {
        if (id.namespace() != NAMESPACE) return null
        return handler.createItem(id.value())
    }

    override fun createItemStack(id: Identifier, amount: Int, player: Player?): ItemStack? {
        return getInternalType(id)?.apply { this.amount = amount }
    }
}