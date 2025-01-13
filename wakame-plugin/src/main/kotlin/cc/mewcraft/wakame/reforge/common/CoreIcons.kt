package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.cells.AbilityCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.item.realize
import org.bukkit.inventory.ItemStack

/**
 * 临时实现, 用来获取一些“随机”的物品类型作为菜单图标.
 */
internal object CoreIcons {
    private const val ICON_ID_PREFIX = "internal:reforge/core_icon"
    private const val DEFAULT_ICON_ID = "$ICON_ID_PREFIX/default"

    fun getNekoStack(core: Core): NekoStack {
        val coreId = core.id.value() // 核心 id, 但去掉 namespace
        val item = when (core) {
            is AttributeCore -> KoishRegistries.ITEM["$ICON_ID_PREFIX/attribute/$coreId"]
            is AbilityCore -> KoishRegistries.ITEM["$ICON_ID_PREFIX/ability/$coreId"]
            is EmptyCore -> KoishRegistries.ITEM["$ICON_ID_PREFIX/empty"]
            else -> KoishRegistries.ITEM[DEFAULT_ICON_ID]
        } ?: getDefaultIcon()
        val stack = item.realize()
        return stack.apply(ItemRenderers.SIMPLE::render)
    }

    fun getItemStack(core: Core): ItemStack {
        return getNekoStack(core).wrapped
    }

    private fun getDefaultIcon(): NekoItem {
        return KoishRegistries.ITEM[DEFAULT_ICON_ID] ?: run {
            LOGGER.error("Default core icon not found! Please fix your config by add a item with id '$DEFAULT_ICON_ID'")
            KoishRegistries.ITEM.getDefaultEntry().value
        }
    }
}