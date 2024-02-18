package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.NekoItemStack
import net.kyori.adventure.text.Component

internal interface NameStylizer {

    /**
     * 为给定的 [NekoItemStack] 生成名字。
     *
     * 不像 [LoreStylizer]，该函数生成的名字可以直接用在物品上。
     *
     * @param item 要生成名字的物品
     * @return 生成的物品名字
     */
    fun stylize(item: NekoItemStack): Component

}