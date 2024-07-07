package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.packet.PacketNekoStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The [ItemRenderer] used to render [PacketNekoStack].
 */
internal class PacketItemRenderer : KoinComponent, ItemRenderer<PacketNekoStack> {
    private val itemModelDataLookup: ItemModelDataLookup by inject()

    override fun render(nekoStack: PacketNekoStack) {
        val lore = TextRenderer.generateLoreLines(nekoStack).flatten()
        val customModelData = itemModelDataLookup[nekoStack.key, nekoStack.variant]
        nekoStack.setLore(lore)
        nekoStack.setCustomModelData(customModelData)
        // 为了麦若，去掉物品的真实根标签
        nekoStack.erase()
    }
}