package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.registry2.DynamicRegistries
import org.bukkit.inventory.ItemStack

/**
 * 临时实现, 用来获取一些“随机”的物品类型作为菜单图标.
 */
internal object CoreIcons {
    private const val ICON_ID_PREFIX = "internal:menu/core_icon"
    private const val DEFAULT_ICON_ID = "$ICON_ID_PREFIX/default"

    fun getNekoStack(core: Core): NekoStack {
        val coreId = core.id.value() // 核心 id, 但去掉 namespace
        val item = when (core) {
            is AttributeCore -> DynamicRegistries.ITEM["$ICON_ID_PREFIX/attribute/$coreId"]
            is EmptyCore -> DynamicRegistries.ITEM["$ICON_ID_PREFIX/empty"]
            else -> DynamicRegistries.ITEM[DEFAULT_ICON_ID]
        } ?: getDefaultIcon()
        val stack = item.realize()
        return stack.apply(ItemRenderers.SIMPLE::render)
    }

    fun getItemStack(core: Core): ItemStack {
        return getNekoStack(core).bukkitStack
    }

    private fun getDefaultIcon(): NekoItem {
        return DynamicRegistries.ITEM[DEFAULT_ICON_ID] ?: run {
            LOGGER.error("Default core icon not found! Please fix your config by add a item with id '$DEFAULT_ICON_ID'")
            DynamicRegistries.ITEM.getDefaultEntry().unwrap()
        }
    }
}