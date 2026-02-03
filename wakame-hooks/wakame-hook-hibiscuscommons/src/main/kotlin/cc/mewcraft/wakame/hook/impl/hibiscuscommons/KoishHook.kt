package cc.mewcraft.wakame.hook.impl.hibiscuscommons

import cc.mewcraft.wakame.item.KoishItemRefHandler
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import me.lojosho.hibiscuscommons.hooks.Hook
import me.lojosho.hibiscuscommons.hooks.HookFlag
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack

class KoishHook : Hook(
    // FIXME 改名为 "koish"
    "wakame", HookFlag.ITEM_SUPPORT, HookFlag.LATE_LOAD
) {

    override fun getItem(itemId: String): ItemStack? {
        return KoishItemRefHandler.createItemStack(Key.key(KOISH_NAMESPACE, itemId), 1, null)
    }

    override fun getItemString(itemStack: ItemStack): String? {
        return KoishItemRefHandler.getId(itemStack)?.value()
    }

    override fun load() {
        this.isActive = true // Koish 物品在 bootstrap 阶段就已经确定, 所以 isActive 可以始终返回 true
    }
}