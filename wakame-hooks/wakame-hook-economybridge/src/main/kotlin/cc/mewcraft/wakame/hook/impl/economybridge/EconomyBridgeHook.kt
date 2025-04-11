package cc.mewcraft.wakame.hook.impl.economybridge

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item2.KoishItemRefHandler
import cc.mewcraft.wakame.util.Identifiers
import org.bukkit.inventory.ItemStack
import su.nightexpress.economybridge.ItemBridge
import su.nightexpress.economybridge.item.handler.AbstractItemHandler

@Hook(plugins = ["EconomyBridge"])
object EconomyBridgeHook : AbstractItemHandler() {

    init {
        ItemBridge.getItemManager().register(this)
    }

    override fun getName(): String {
        return KoishItemRefHandler.systemName
    }

    override fun canHandle(p0: ItemStack): Boolean {
        return KoishItemRefHandler.getId(p0) != null
    }

    override fun createItem(p0: String): ItemStack? {
        val id = Identifiers.tryParse(p0) ?: return null
        return KoishItemRefHandler.createItemStack(id, 1, null)
    }

    override fun getItemId(p0: ItemStack): String? {
        return KoishItemRefHandler.getId(p0)?.asString()
    }

    override fun isValidId(p0: String): Boolean {
        val id = Identifiers.of(p0)
        return KoishItemRefHandler.accepts(id)
    }

}