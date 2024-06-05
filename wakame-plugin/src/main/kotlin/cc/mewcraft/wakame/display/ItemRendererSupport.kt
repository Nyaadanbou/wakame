package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.ShowNekoStack
import cc.mewcraft.wakame.lookup.ItemModelDataLookup
import cc.mewcraft.wakame.packet.PacketNekoStack
import cc.mewcraft.wakame.util.backingCustomModelData
import cc.mewcraft.wakame.util.backingCustomName
import cc.mewcraft.wakame.util.backingLore
import cc.mewcraft.wakame.util.packetevents.backingCustomModelData
import cc.mewcraft.wakame.util.packetevents.backingCustomName
import cc.mewcraft.wakame.util.packetevents.backingLore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The [ItemRenderer] used to render [PlayNekoStacks][PlayNekoStack].
 *
 * @property textStylizer
 * @property loreFinalizer
 */
internal class PlayItemRenderer(
    // FIXME 不再使用
    private val textStylizer: TextStylizer,
    private val loreFinalizer: LoreFinalizer,
) : KoinComponent, ItemRenderer<PlayNekoStack> {
    private val modelLookup: ItemModelDataLookup by inject()

    override fun render(nekoStack: PlayNekoStack) {
        val customName = textStylizer.stylizeName(nekoStack).content
        val lore = textStylizer.stylizeLore(nekoStack).let(loreFinalizer::finalize)
        val customModelData = modelLookup[nekoStack.key, nekoStack.variant]

        // directly edit the backing ItemMeta to avoid cloning
        nekoStack.itemStack.apply {
            backingCustomName = customName
            backingLore = lore
            backingCustomModelData = customModelData
        }

        // 为了麦若，去掉物品的真实根标签
        nekoStack.erase()
    }
}

/**
 * The [ItemRenderer] used to render [ShowNekoStacks][ShowNekoStack].
 *
 * @property textStylizer
 * @property loreFinalizer
 */
internal class ShowItemRenderer(
    private val textStylizer: TextStylizer,
    private val loreFinalizer: LoreFinalizer,
) : ItemRenderer<ShowNekoStack> {
    override fun render(nekoStack: ShowNekoStack) {
        TODO("Not yet implemented")
    }
}

/**
 * The [ItemRenderer] used to render [PacketNekoStacks][PacketNekoStack].
 *
 * @property textStylizer
 * @property loreFinalizer
 */
internal class PacketItemRenderer(
    private val textStylizer: TextStylizer,
    private val loreFinalizer: LoreFinalizer,
) : KoinComponent, ItemRenderer<PacketNekoStack> {
    private val modelLookup: ItemModelDataLookup by inject()

    override fun render(nekoStack: PacketNekoStack) {
        val customName = textStylizer.stylizeName(nekoStack).content
        val lore = textStylizer.stylizeLore(nekoStack).let(loreFinalizer::finalize)
        val customModelData = modelLookup[nekoStack.key, nekoStack.variant]

        nekoStack.itemStack.apply {
            backingCustomName = customName
            backingLore = lore
            backingCustomModelData = customModelData
        }

        // 为了麦若，去掉物品的真实根标签
        nekoStack.erase()
    }
}