package cc.mewcraft.wakame.hook.impl.breweryx

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item2.KoishItemRefHandler
import com.dre.brewery.recipe.PluginItem
import org.bukkit.inventory.ItemStack

@Hook(plugins = ["BreweryX"])
object BreweryXHook {

    init {
        PluginItem.registerForConfig("koish", ::KoishPluginItem)
    }

}

class KoishPluginItem : PluginItem() {

    override fun matches(p0: ItemStack): Boolean {
        val id = KoishItemRefHandler.getId(p0) ?: return false
        return itemId.equals(id.value(), ignoreCase = true)
    }

}