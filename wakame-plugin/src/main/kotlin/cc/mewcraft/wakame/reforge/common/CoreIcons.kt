package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.NekoItemHolder
import cc.mewcraft.wakame.display2.implementation.simple.SimpleItemRendererContext
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.EmptyCore
import cc.mewcraft.wakame.item.components.cells.SkillCore
import org.bukkit.inventory.ItemStack

/**
 * 临时实现, 用来获取一些“随机”的物品类型作为菜单图标.
 */
internal object CoreIcons {
    private const val ITEM_ID_PREFIX = "internal:reforge/core_icon"
    private const val DEFAULT_ITEM_ID = "$ITEM_ID_PREFIX/default"

    fun getNekoStack(core: Core): NekoStack {
        val coreId = core.id.value() // 核心 id, 但去掉 namespace
        val holder = when (core) {
            is AttributeCore -> NekoItemHolder.getOrDefault("$ITEM_ID_PREFIX/attribute/$coreId", DEFAULT_ITEM_ID)
            is SkillCore -> NekoItemHolder.getOrDefault("$ITEM_ID_PREFIX/skill/$coreId", DEFAULT_ITEM_ID)
            is EmptyCore -> NekoItemHolder.getOrDefault("$ITEM_ID_PREFIX/empty", DEFAULT_ITEM_ID)
            else -> NekoItemHolder.get(DEFAULT_ITEM_ID)
        }
        val nekoStack = holder.createNekoStack()
        ItemRenderers.SIMPLE.render(nekoStack, SimpleItemRendererContext)
        return nekoStack
    }

    fun getItemStack(core: Core): ItemStack {
        return getNekoStack(core).itemStack
    }
}