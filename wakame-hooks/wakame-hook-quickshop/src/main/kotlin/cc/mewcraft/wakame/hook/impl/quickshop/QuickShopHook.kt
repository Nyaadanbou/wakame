package cc.mewcraft.wakame.hook.impl.quickshop

import cc.mewcraft.wakame.PluginHolder
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item2.isKoish
import cc.mewcraft.wakame.item2.typeId
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import com.ghostchu.quickshop.api.QuickShopAPI
import com.ghostchu.quickshop.api.registry.BuiltInRegistry
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionHandler
import com.ghostchu.quickshop.api.registry.builtin.itemexpression.ItemExpressionRegistry
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

@Hook(plugins = ["QuickShop-Hikari"])
object QuickShopHook : Listener, ItemExpressionHandler {
    val quickShop: QuickShopAPI = QuickShopAPI.getInstance()

    init {
        val registry = quickShop.registry.getRegistry(BuiltInRegistry.ITEM_EXPRESSION)
        if (registry is ItemExpressionRegistry) {
            registry.registerHandlerSafely(this)
        }
    }

    override fun getPlugin(): Plugin {
        return PluginHolder.INSTANCE
    }

    override fun getPrefix(): String? {
        return KOISH_NAMESPACE
    }

    override fun match(itemStack: ItemStack, expression: String): Boolean {
        if (!itemStack.isKoish)
            return false
        val itemId = itemStack.typeId
        return expression == itemId.asString()
    }
}