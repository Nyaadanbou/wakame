package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.item2.KoishStackGenerator
import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import org.bukkit.inventory.ItemStack

/**
 * 临时实现, 用来获取一些“随机”的物品类型作为菜单图标.
 */
internal object CoreIcons2 {
    private const val ICON_ID_PREFIX = "internal/menu/core_icon"
    private const val DEFAULT_ICON_ID = "$ICON_ID_PREFIX/default"

    fun getItemStack(core: Core): ItemStack {
        val coreId = core.id.value() // 核心 id, 但去掉 namespace
        val item = when (core) {
            is AttributeCore -> BuiltInRegistries.ITEM["$ICON_ID_PREFIX/attribute/$coreId"]
            is EmptyCore -> BuiltInRegistries.ITEM["$ICON_ID_PREFIX/empty"]
            else -> BuiltInRegistries.ITEM[DEFAULT_ICON_ID]
        } ?: getDefaultIcon()
        val stack = KoishStackGenerator.generate(item, Context(item))
        return stack
    }

    private fun getDefaultIcon(): KoishItem {
        return BuiltInRegistries.ITEM[DEFAULT_ICON_ID] ?: run {
            LOGGER.error("Default core icon not found! Please fix your config by add a item with id '$DEFAULT_ICON_ID'")
            BuiltInRegistries.ITEM.getDefaultEntry().unwrap()
        }
    }
}