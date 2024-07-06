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
        // TODO 1.20.5 之后, `minecraft:custom_data` 不会发送到客户端. 是不是不需要显式的移除 NBT 标签了?
        // 为了麦若，去掉物品的真实根标签
        nekoStack.erase()
    }
}