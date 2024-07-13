package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.packet.PacketNekoStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The [ItemRenderer] used to render [PacketNekoStack].
 */
internal class PacketItemRenderer : KoinComponent, ItemRenderer<PacketNekoStack> {
    private val loreLineFlatter: LoreLineFlatter by inject()
    private val itemModelDataLookup: ItemModelDataLookup by inject()

    override fun render(nekoStack: PacketNekoStack) {
        val loreLines = TextRenderer.generateLoreLines(nekoStack)
        val lore = loreLineFlatter.flatten(loreLines) // flatten the lore lines
        val customModelData = itemModelDataLookup[nekoStack.key, nekoStack.variant]

        nekoStack.lore(lore)
        nekoStack.customModelData(customModelData)
        nekoStack.erase() // 为了麦若, 去掉 `minecraft:custom_data`
    }
}