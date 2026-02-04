package cc.mewcraft.wakame.hook.impl.nightcore

import cc.mewcraft.wakame.item.KoishItemRefHandler
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter
import su.nightexpress.nightcore.integration.item.data.ItemIdData

class KoishItemAdapter : IdentifiableItemAdapter("koish") {

    override fun getItemId(item: ItemStack): String? {
        return KoishItemRefHandler.getId(item)?.value()
    }

    override fun createItem(itemId: String): ItemStack? {
        return KoishItemRefHandler.createItemStack(Key.key(KOISH_NAMESPACE, itemId), 1, null)
    }

    override fun canHandle(item: ItemStack): Boolean {
        return KoishItemRefHandler.getId(item) != null
    }

    override fun canHandle(data: ItemIdData): Boolean {
        return KoishItemRefHandler.accepts(Key.key(KOISH_NAMESPACE, data.itemId))
    }
}