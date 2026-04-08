package cc.mewcraft.wakame.item.mixin

import cc.mewcraft.wakame.bridge.ExtraDataComponents
import cc.mewcraft.wakame.item.data.ItemDataContainer
import net.minecraft.world.item.ItemStack

fun ItemStack.getItemKey(): ItemKey? {
    return get(ExtraDataComponents.ITEM_KEY) as? ItemKey
}

fun ItemStack.getDataContainer(): ItemDataContainer? {
    return get(ExtraDataComponents.DATA_CONTAINER) as? ItemDataContainer
}

fun ItemStack.getDataContainerOrDefault(default: ItemDataContainer): ItemDataContainer {
    return getOrDefault(ExtraDataComponents.DATA_CONTAINER, default) as ItemDataContainer
}