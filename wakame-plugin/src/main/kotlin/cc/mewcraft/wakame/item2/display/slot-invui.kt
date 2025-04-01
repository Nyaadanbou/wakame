package cc.mewcraft.wakame.item2.display

import xyz.xenondevs.invui.item.ItemWrapper

/**
 * 解析得到一个 [ItemWrapper], 该物品已经应用了解析后的所有数据.
 */
fun SlotDisplay.resolveToItemWrapper(dsl: SlotDisplayLoreData.LineConfig.Builder.() -> Unit = {}): ItemWrapper {
    return ItemWrapper(resolveToItemStack(dsl))
}
