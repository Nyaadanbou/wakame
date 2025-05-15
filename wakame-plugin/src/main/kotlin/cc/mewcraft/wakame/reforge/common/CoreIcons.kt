package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.item2.KoishStackGenerator
import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.item2.data.impl.AttributeCore
import cc.mewcraft.wakame.item2.data.impl.Core
import cc.mewcraft.wakame.item2.data.impl.EmptyCore
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import org.bukkit.inventory.ItemStack

/**
 * 临时实现, 用来获取一些“随机”的物品类型作为菜单图标.
 */
internal object CoreIcons {
    private const val ICON_ID_PREFIX = "internal:menu/core_icon"
    private const val DEFAULT_ICON_ID = "$ICON_ID_PREFIX/default"

    fun getItemStack(coreId: String, core: Core): ItemStack {
        val item = when (core) {
            is AttributeCore -> BuiltInRegistries.ITEM["$ICON_ID_PREFIX/attribute/$coreId"]
            is EmptyCore -> BuiltInRegistries.ITEM["$ICON_ID_PREFIX/empty"]
            else -> BuiltInRegistries.ITEM[DEFAULT_ICON_ID]
        } ?: getDefaultIcon()
        val stack = KoishStackGenerator.generate(item, Context(item))
        return stack.apply(ItemRenderers.SIMPLE::render)
    }

    private fun getDefaultIcon(): KoishItem {
        return BuiltInRegistries.ITEM[DEFAULT_ICON_ID] ?: run {
            LOGGER.error("Default core icon not found! Please fix your config by add a item with id '$DEFAULT_ICON_ID'")
            BuiltInRegistries.ITEM.getDefaultEntry().unwrap()
        }
    }
}