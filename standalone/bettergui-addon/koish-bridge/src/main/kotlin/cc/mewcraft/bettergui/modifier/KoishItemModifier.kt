package cc.mewcraft.bettergui.modifier

import cc.mewcraft.wakame.item.KoishItemRefHandler
import cc.mewcraft.wakame.util.KoishKeys
import me.hsgamer.hscore.common.StringReplacer
import me.hsgamer.hscore.minecraft.item.ItemModifier
import org.bukkit.inventory.ItemStack
import java.util.*

class KoishItemModifier : ItemModifier<ItemStack> {
    private lateinit var itemId: String

    override fun modify(original: ItemStack, uuid: UUID?, stringReplacer: StringReplacer): ItemStack {
        if (!::itemId.isInitialized) return original
        val itemId2 = stringReplacer.replaceOrOriginal(itemId, uuid)
        val identifier = KoishKeys.tryParse(itemId2) ?: return original
        val itemstack = KoishItemRefHandler.createItemStack(identifier, 1, null) ?: return original
        return itemstack
    }

    override fun toObject(): Any? {
        return if (::itemId.isInitialized) itemId else null
    }

    override fun loadFromObject(`object`: Any?) {
        this.itemId = `object`.toString()
    }
}