package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.packet.PacketNekoStack
import cc.mewcraft.wakame.util.packetevents.backingCustomModelData
import cc.mewcraft.wakame.util.packetevents.backingLore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The [ItemRenderer] used to render [PacketNekoStacks][PacketNekoStack].
 *
 * @property renderer
 */
internal class PacketItemRenderer(
    private val renderer: TextRenderer,
) : KoinComponent, ItemRenderer<PacketNekoStack> {
    private val modelLookup: ItemModelDataLookup by inject()

    override fun render(nekoStack: PacketNekoStack) {
        val lore = renderer.generateLoreLines(nekoStack).flatten()
        val customModelData = modelLookup[nekoStack.key, nekoStack.variant]

        nekoStack.stack.apply {
            backingLore = lore
            backingCustomModelData = customModelData
        }

        // TODO 1.20.5 之后, `minecraft:custom_data` 不会发送到客户端. 是不是不需要显式的移除 NBT 标签了?
        // 为了麦若，去掉物品的真实根标签
        nekoStack.erase()
    }
}