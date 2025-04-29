package cc.mewcraft.wakame.hook.impl.breweryx

import cc.mewcraft.wakame.item2.KoishItemRefHandler
import com.dre.brewery.recipe.PluginItem
import org.bukkit.inventory.ItemStack

/**
 * 实现了 [PluginItem] 以便让 BreweryX 识别 Koish 的物品.
 *
 * ### 物品格式
 * - "koish:example" -> id 为 example 的 Koish 物品
 */
class KoishPluginItem : PluginItem() {

    override fun matches(p0: ItemStack): Boolean {
        val id = KoishItemRefHandler.getId(p0) ?: return false
        return itemId.equals(id.value(), ignoreCase = true)
    }
}